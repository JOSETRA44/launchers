package com.tien.tensor.data.arsenal

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.tien.tensor.domain.model.Finding
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.domain.port.SecurityModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

/**
 * Deep device-posture audit. Strictly read-only: every check inspects the
 * user's own device state through public APIs and reports it — nothing is
 * changed, bypassed or probed externally.
 */
class DeviceIntegrityModule(private val context: Context) : SecurityModule {

    override val meta = ModuleMeta(
        id          = "integrity",
        name        = "INTEGRITY+",
        tagline     = "Lock · encryption · root · debug surface",
        isStreaming = false
    )

    override fun observe(): Flow<ModuleReport> =
        flow { emit(scan()) }.flowOn(Dispatchers.IO)

    private fun scan(): ModuleReport {
        val findings = mutableListOf<Finding>()

        // ── Screen lock ───────────────────────────────────────────────────────
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        findings += if (km.isDeviceSecure) {
            Finding("int_lock", "SCREEN LOCK", "PIN/pattern/password protection is active.", Severity.INFO)
        } else {
            Finding("int_lock", "NO SCREEN LOCK", "Anyone with physical access owns this device. Set a PIN/password.", Severity.CRITICAL)
        }

        // ── Storage encryption ────────────────────────────────────────────────
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val encrypted = dpm.storageEncryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
            dpm.storageEncryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER
        findings += if (encrypted) {
            Finding("int_enc", "STORAGE ENCRYPTED", "User data is encrypted at rest.", Severity.INFO)
        } else {
            Finding("int_enc", "STORAGE NOT ENCRYPTED", "Data at rest is readable if the device is seized.", Severity.CRITICAL)
        }

        // ── Root indicators ───────────────────────────────────────────────────
        val suHit = SU_PATHS.firstOrNull { File(it).exists() }
        val testKeys = Build.TAGS?.contains("test-keys") == true
        findings += when {
            suHit != null -> Finding("int_root", "ROOT BINARY FOUND", "su present at $suHit — system partition integrity is broken.", Severity.CRITICAL)
            testKeys      -> Finding("int_root", "TEST-KEYS BUILD", "OS signed with test keys — not a production build.", Severity.HIGH)
            else          -> Finding("int_root", "ROOT INDICATORS", "No su binaries or test-key signatures detected.", Severity.INFO)
        }

        // ── Debug surface ─────────────────────────────────────────────────────
        val adbOn = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        findings += if (adbOn) {
            Finding("int_adb", "USB DEBUGGING ENABLED", "ADB grants shell access to anyone who plugs in while unlocked.", Severity.MEDIUM)
        } else {
            Finding("int_adb", "USB DEBUGGING", "ADB is disabled.", Severity.INFO)
        }

        val devOn = Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        findings += if (devOn) {
            Finding("int_dev", "DEV OPTIONS ENABLED", "Developer settings expose extra attack surface.", Severity.LOW)
        } else {
            Finding("int_dev", "DEV OPTIONS", "Developer settings are off.", Severity.INFO)
        }

        // ── Side-install surface ──────────────────────────────────────────────
        val installerCount = countInstallRequesters()
        findings += Finding(
            "int_installers",
            "PACKAGE INSTALL SURFACE",
            "$installerCount app(s) declare REQUEST_INSTALL_PACKAGES and may prompt to sideload APKs.",
            if (installerCount > 3) Severity.LOW else Severity.INFO
        )

        val worst = findings.maxByOrNull { it.severity.ordinal }?.severity ?: Severity.INFO
        return ModuleReport(
            moduleId  = meta.id,
            headline  = "${findings.size} CHECKS · WORST: $worst",
            findings  = findings.sortedByDescending { it.severity.ordinal },
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun countInstallRequesters(): Int = try {
        @Suppress("DEPRECATION")
        context.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            .count { it.requestedPermissions?.contains("android.permission.REQUEST_INSTALL_PACKAGES") == true }
    } catch (e: Exception) {
        0
    }

    private companion object {
        val SU_PATHS = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/su", "/su/bin/su", "/data/local/bin/su"
        )
    }
}
