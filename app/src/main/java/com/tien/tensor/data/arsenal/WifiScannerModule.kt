package com.tien.tensor.data.arsenal

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import com.tien.tensor.domain.model.Finding
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.domain.port.SecurityModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Passive WiFi security reconnaissance.
 *
 * Analyses the current connection (signal, band, link speed) using
 * non-location APIs, then attempts to enumerate nearby networks via
 * [WifiManager.getScanResults]. Scan results require ACCESS_FINE_LOCATION
 * on API 23+ — the module degrades gracefully without it, showing only
 * the current-link analysis. Never transmits any probes.
 */
class WifiScannerModule(private val context: Context) : SecurityModule {

    override val meta = ModuleMeta(
        id          = "wifi_scan",
        name        = "WIFI RECON",
        tagline     = "Near-field enumeration · signal · security flags",
        isStreaming = false
    )

    @Suppress("DEPRECATION")
    override fun observe(): Flow<ModuleReport> = flow {
        val findings = mutableListOf<Finding>()
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val onWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val onVpn  = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

        if (onVpn) findings += Finding("wifi_vpn", "VPN ACTIVE", "Traffic is tunnelled through a VPN.", Severity.INFO)

        if (onWifi) {
            val info = wm.connectionInfo
            val rssi  = info.rssi
            val level = WifiManager.calculateSignalLevel(rssi, 5)
            val bars  = "█".repeat(level) + "░".repeat(4 - level.coerceAtMost(4))
            val label = arrayOf("UNUSABLE", "WEAK", "FAIR", "GOOD", "EXCELLENT")[level.coerceIn(0, 4)]
            findings += Finding("wifi_sig", "SIGNAL", "$bars  $label · $rssi dBm",
                if (level >= 3) Severity.INFO else Severity.LOW)

            val speed = info.linkSpeed
            if (speed > 0) findings += Finding("wifi_speed", "LINK SPEED", "$speed Mbps", Severity.INFO)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val freq = info.frequency
                if (freq > 0) {
                    val band = when { freq < 3000 -> "2.4 GHz (congested)"; freq < 6000 -> "5 GHz"; else -> "6 GHz (Wi-Fi 6E)" }
                    findings += Finding("wifi_band", "BAND", "$freq MHz · $band", Severity.INFO)
                }
            }
        } else {
            findings += Finding("wifi_off", "NOT ON WIFI",
                "Cellular or Ethernet active. Connect to WiFi for full analysis.", Severity.INFO)
        }

        // Scan results — requires location permission; degrade gracefully
        val scans = try { wm.scanResults.orEmpty() } catch (_: Exception) { emptyList() }
        if (scans.isNotEmpty()) {
            val open = scans.filter { it.capabilities.isNullOrBlank() || it.capabilities == "[ESS]" }
            val wep  = scans.filter { it.capabilities?.contains("WEP") == true }
            val wpa3 = scans.filter { it.capabilities?.contains("SAE") == true }
            findings += Finding("wifi_count", "NETWORKS IN RANGE",
                "${scans.size} total · ${wpa3.size} WPA3 · ${open.size} OPEN · ${wep.size} WEP", Severity.INFO)
            if (open.isNotEmpty()) findings += Finding("wifi_open", "OPEN NETWORKS NEARBY",
                open.take(4).joinToString(" · ") { "\"${it.SSID}\"" }, Severity.MEDIUM)
            if (wep.isNotEmpty()) findings += Finding("wifi_wep", "WEP NETWORKS DETECTED",
                "${wep.size} AP(s) using WEP — protocol is cryptographically broken", Severity.HIGH)
        } else {
            findings += Finding("wifi_noscan", "SCAN UNAVAILABLE",
                "Location permission required on Android 6+ for network enumeration.", Severity.INFO)
        }

        val hdr = if (onWifi) "ON WIFI · ${scans.size} VISIBLE · ${wm.connectionInfo.rssi} dBm"
        else "NOT ON WIFI · ${scans.size} VISIBLE"
        emit(ModuleReport(meta.id, hdr, findings, System.currentTimeMillis()))
    }.flowOn(Dispatchers.IO)
}
