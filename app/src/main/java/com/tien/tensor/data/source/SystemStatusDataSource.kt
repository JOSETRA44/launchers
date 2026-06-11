package com.tien.tensor.data.source

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import com.tien.tensor.domain.model.NetworkType
import com.tien.tensor.domain.model.SystemStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class SystemStatusDataSource(private val context: Context) {

    val systemStatus: Flow<SystemStatus> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun readBattery(): Pair<Int, Boolean> {
            val i = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level  = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
            val scale  = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val status = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val pct    = if (scale > 0) (level * 100f / scale).toInt() else 0
            val chg    = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL
            return Pair(pct, chg)
        }

        fun readNet(): NetworkType {
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return NetworkType.NONE
            return when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> NetworkType.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
                else -> NetworkType.NONE
            }
        }

        var lastBat = readBattery()
        var lastNet = readNet()

        fun push() { trySend(SystemStatus(lastBat.first, lastBat.second, lastNet)) }
        push()

        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                lastBat = readBattery()
                push()
            }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val netCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network)  { lastNet = readNet(); push() }
            override fun onLost(network: Network)       { lastNet = NetworkType.NONE; push() }
            override fun onCapabilitiesChanged(n: Network, nc: NetworkCapabilities) { lastNet = readNet(); push() }
        }
        cm.registerDefaultNetworkCallback(netCallback)

        awaitClose {
            context.unregisterReceiver(batteryReceiver)
            cm.unregisterNetworkCallback(netCallback)
        }
    }.flowOn(Dispatchers.IO)
}
