package com.tien.tensor.data.source

import android.app.KeyguardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import com.tien.tensor.domain.model.CheckStatus
import com.tien.tensor.domain.model.SecurityCheck
import java.io.File

/**
 * Read-only, on-device security audit. Every check is informational —
 * nothing here modifies system state.
 */
class SecurityDataSource(private val context: Context) {

    fun runChecks(): List<SecurityCheck> = listOf(
        screenLockCheck(),
        rootCheck(),
        adbCheck(),
        devOptionsCheck(),
        vpnCheck()
    )

    private fun screenLockCheck(): SecurityCheck {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val secure   = keyguard.isDeviceSecure
        return SecurityCheck(
            id     = "screen_lock",
            label  = "SCREEN LOCK",
            status = if (secure) CheckStatus.PASS else CheckStatus.WARN,
            detail = if (secure) "PIN/pattern/biometric active" else "No secure lock configured"
        )
    }

    private fun rootCheck(): SecurityCheck {
        val rooted = SU_PATHS.any { runCatching { File(it).exists() }.getOrDefault(false) }
        return SecurityCheck(
            id     = "root",
            label  = "ROOT ACCESS",
            status = if (rooted) CheckStatus.WARN else CheckStatus.PASS,
            detail = if (rooted) "su binary detected" else "No su binary found"
        )
    }

    private fun adbCheck(): SecurityCheck {
        val enabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        return SecurityCheck(
            id     = "adb",
            label  = "USB DEBUGGING",
            status = if (enabled) CheckStatus.WARN else CheckStatus.PASS,
            detail = if (enabled) "ADB is enabled" else "ADB is disabled"
        )
    }

    private fun devOptionsCheck(): SecurityCheck {
        val enabled = Settings.Global.getInt(
            context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) == 1
        return SecurityCheck(
            id     = "dev_options",
            label  = "DEV OPTIONS",
            status = if (enabled) CheckStatus.INFO else CheckStatus.PASS,
            detail = if (enabled) "Developer options enabled" else "Developer options disabled"
        )
    }

    private fun vpnCheck(): SecurityCheck {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val vpn = active?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        return SecurityCheck(
            id     = "vpn",
            label  = "VPN TUNNEL",
            status = CheckStatus.INFO,
            detail = if (vpn) "Traffic routed through VPN" else "No VPN active"
        )
    }

    private companion object {
        val SU_PATHS = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/sd/xbin/su", "/data/local/xbin/su", "/data/local/bin/su"
        )
    }
}
