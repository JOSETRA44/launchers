// Deliberately touches deprecated telephony surface: legacy radio constants
// (CDMA/EVDO/iDEN) are still what old devices report, and PhoneStateListener
// is the only signal-strength API below Android 12.
@file:Suppress("DEPRECATION")

package com.tien.tensor.data.source

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.tien.tensor.domain.model.NetworkType
import com.tien.tensor.domain.model.SystemStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Reads the real hardware state behind the dynamic status bar.
 * Everything is event-driven (broadcasts + system callbacks) — no polling.
 *
 * Cellular generation (5G/4G/...) needs READ_PHONE_STATE; without the grant
 * we degrade to the generic CELLULAR type. Signal level listening and Wi-Fi
 * RSSI require no dangerous permissions.
 */
class SystemStatusDataSource(private val context: Context) {

    val systemStatus: Flow<SystemStatus> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        var battery   = readBattery()
        var powerSave = pm.isPowerSaveMode
        var network   = classify(cm.getNetworkCapabilities(cm.activeNetwork), tm)
        var wifiLevel = wifiLevel(cm.getNetworkCapabilities(cm.activeNetwork))
        var cellLevel = -1

        fun push() {
            val level = when (network) {
                NetworkType.WIFI -> wifiLevel
                NetworkType.NONE, NetworkType.ETHERNET -> -1
                else -> cellLevel
            }
            trySend(SystemStatus(battery.first, battery.second, powerSave, network, level))
        }
        push()

        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) { battery = readBattery(); push() }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val powerSaveReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) { powerSave = pm.isPowerSaveMode; push() }
        }
        context.registerReceiver(powerSaveReceiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))

        val netCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(n: Network) {
                val caps = cm.getNetworkCapabilities(n)
                network = classify(caps, tm); wifiLevel = wifiLevel(caps); push()
            }
            override fun onLost(n: Network) { network = NetworkType.NONE; push() }
            override fun onCapabilitiesChanged(n: Network, caps: NetworkCapabilities) {
                network = classify(caps, tm); wifiLevel = wifiLevel(caps); push()
            }
        }
        cm.registerDefaultNetworkCallback(netCallback)

        // Cellular signal level — listening requires no permission on any API level
        var telephonyCallback: TelephonyCallback? = null
        var phoneStateListener: PhoneStateListener? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val cb = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                override fun onSignalStrengthsChanged(s: SignalStrength) { cellLevel = s.level; push() }
            }
            tm.registerTelephonyCallback(ContextCompat.getMainExecutor(context), cb)
            telephonyCallback = cb
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Deprecated("Pre-S path")
                override fun onSignalStrengthsChanged(s: SignalStrength) { cellLevel = s.level; push() }
            }
            @Suppress("DEPRECATION")
            tm.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
            phoneStateListener = listener
        }

        awaitClose {
            context.unregisterReceiver(batteryReceiver)
            context.unregisterReceiver(powerSaveReceiver)
            cm.unregisterNetworkCallback(netCallback)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyCallback?.let { tm.unregisterTelephonyCallback(it) }
            } else {
                @Suppress("DEPRECATION")
                phoneStateListener?.let { tm.listen(it, PhoneStateListener.LISTEN_NONE) }
            }
        }
    }.flowOn(Dispatchers.IO)

    // ── Battery ───────────────────────────────────────────────────────────────

    private fun readBattery(): Pair<Int, Boolean> {
        val i = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level  = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale  = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val pct    = if (scale > 0) (level * 100f / scale).toInt() else 0
        val chg    = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                     status == BatteryManager.BATTERY_STATUS_FULL
        return Pair(pct, chg)
    }

    // ── Network classification ────────────────────────────────────────────────

    private fun classify(caps: NetworkCapabilities?, tm: TelephonyManager): NetworkType = when {
        caps == null                                                -> NetworkType.NONE
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)       -> NetworkType.WIFI
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)   -> NetworkType.ETHERNET
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)   -> cellularGeneration(tm)
        else                                                        -> NetworkType.NONE
    }

    private fun cellularGeneration(tm: TelephonyManager): NetworkType {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return NetworkType.CELLULAR
        return try {
            when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_NR    -> NetworkType.CELL_5G
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN -> NetworkType.CELL_4G
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> NetworkType.CELL_3G
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_GSM,
                TelephonyManager.NETWORK_TYPE_IDEN  -> NetworkType.CELL_2G
                else                                -> NetworkType.CELLULAR
            }
        } catch (e: SecurityException) {
            NetworkType.CELLULAR
        }
    }

    // ── Wi-Fi signal ──────────────────────────────────────────────────────────

    private fun wifiLevel(caps: NetworkCapabilities?): Int {
        if (caps == null || !caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return -1
        val rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && caps.signalStrength != Int.MIN_VALUE) {
            caps.signalStrength
        } else {
            @Suppress("DEPRECATION")
            (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .connectionInfo?.rssi ?: Int.MIN_VALUE
        }
        return rssiToLevel(rssi)
    }

    private fun rssiToLevel(rssi: Int): Int = when {
        rssi == Int.MIN_VALUE -> -1
        rssi >= -55 -> 4
        rssi >= -66 -> 3
        rssi >= -77 -> 2
        rssi >= -88 -> 1
        else        -> 0
    }
}
