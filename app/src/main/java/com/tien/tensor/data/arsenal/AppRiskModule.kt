package com.tien.tensor.data.arsenal

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
 * Read-only permission heuristics over every installed package.
 *
 * Scores each app by the *granted* dangerous-permission groups it holds plus
 * risk multipliers (sideloaded install source, debuggable build, ancient
 * targetSdk, surveillance-capable permission combos). Pure analysis of the
 * user's own device — nothing is modified or transmitted.
 */
class AppRiskModule(private val context: Context) : SecurityModule {

    override val meta = ModuleMeta(
        id          = "app_risk",
        name        = "APP RISK",
        tagline     = "Permission heuristics · install source · target SDK",
        isStreaming = false
    )

    override fun observe(): Flow<ModuleReport> =
        flow { emit(scan()) }.flowOn(Dispatchers.Default)

    private fun scan(): ModuleReport {
        val pm = context.packageManager

        @Suppress("DEPRECATION")
        val packages: List<PackageInfo> = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        var flagged = 0
        var sideloadedCount = 0
        val findings = mutableListOf<Finding>()

        for (pkg in packages) {
            val app = pkg.applicationInfo ?: continue
            if (pkg.packageName == context.packageName) continue

            val isSystem    = app.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val isDebuggable = app.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            val sideloaded  = !isSystem && installerOf(pm, pkg.packageName) !in KNOWN_STORES
            if (sideloaded) sideloadedCount++

            val groups = grantedGroups(pkg)
            var score  = groups.size
            val tags   = mutableListOf<String>()

            if (sideloaded)   { score += 3; tags += "SIDELOADED" }
            if (isDebuggable) { score += 2; tags += "DEBUGGABLE" }
            if (app.targetSdkVersion < OLD_TARGET_SDK) { score += 2; tags += "TARGET<${OLD_TARGET_SDK}" }
            if ("CAM" in groups && "MIC" in groups && "LOC" in groups) { score += 3; tags += "SURVEILLANCE-COMBO" }
            if (sideloaded && ("SMS" in groups || "CALLS" in groups)) { score += 2; tags += "SMS/CALL+SIDELOAD" }

            val severity = when {
                score >= 9 -> Severity.CRITICAL
                score >= 7 -> Severity.HIGH
                score >= 5 -> Severity.MEDIUM
                score >= 3 -> Severity.LOW
                else       -> null
            } ?: continue

            flagged++
            val label = try { pm.getApplicationLabel(app).toString() } catch (e: Exception) { pkg.packageName }
            findings += Finding(
                id       = "risk_${pkg.packageName}",
                title    = "$label  [R:$score]",
                detail   = (tags + groups).joinToString(" · ").ifBlank { "low-signal flags" },
                severity = severity
            )
        }

        findings.sortByDescending { it.severity.ordinal }
        findings += Finding(
            id       = "risk_summary_sideload",
            title    = "INSTALL SOURCES",
            detail   = "$sideloadedCount package(s) not installed from a known store",
            severity = if (sideloadedCount > 0) Severity.LOW else Severity.INFO
        )

        return ModuleReport(
            moduleId  = meta.id,
            headline  = "${packages.size} PKGS ANALYZED · $flagged FLAGGED · $sideloadedCount SIDELOADED",
            findings  = findings,
            updatedAt = System.currentTimeMillis()
        )
    }

    /** Granted dangerous-permission groups, as short tags. */
    private fun grantedGroups(pkg: PackageInfo): List<String> {
        val requested = pkg.requestedPermissions ?: return emptyList()
        val flags     = pkg.requestedPermissionsFlags ?: return emptyList()
        val granted   = HashSet<String>()
        for (i in requested.indices) {
            if (flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) granted += requested[i]
        }
        return PERMISSION_GROUPS.filter { (_, perms) -> perms.any { it in granted } }.map { it.key }
    }

    private fun installerOf(pm: PackageManager, packageName: String): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pm.getInstallSourceInfo(packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            pm.getInstallerPackageName(packageName)
        }
    } catch (e: Exception) {
        null
    }

    private companion object {
        const val OLD_TARGET_SDK = 26

        val KNOWN_STORES = setOf(
            "com.android.vending",            // Google Play
            "com.google.android.packageinstaller",
            "com.amazon.venezia",
            "com.huawei.appmarket",
            "com.sec.android.app.samsungapps",
            "com.xiaomi.mipicks",
            "com.oppo.market",
            "com.vivo.appstore",
            "org.fdroid.fdroid"
        )

        val PERMISSION_GROUPS: Map<String, Set<String>> = mapOf(
            "CAM"      to setOf("android.permission.CAMERA"),
            "MIC"      to setOf("android.permission.RECORD_AUDIO"),
            "LOC"      to setOf(
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_BACKGROUND_LOCATION"
            ),
            "SMS"      to setOf(
                "android.permission.READ_SMS",
                "android.permission.SEND_SMS",
                "android.permission.RECEIVE_SMS"
            ),
            "CONTACTS" to setOf("android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS"),
            "CALLS"    to setOf(
                "android.permission.READ_CALL_LOG",
                "android.permission.WRITE_CALL_LOG",
                "android.permission.CALL_PHONE"
            ),
            "PHONE"    to setOf("android.permission.READ_PHONE_STATE", "android.permission.READ_PHONE_NUMBERS"),
            "CALENDAR" to setOf("android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR"),
            "STORAGE"  to setOf(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MANAGE_EXTERNAL_STORAGE",
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VIDEO",
                "android.permission.READ_MEDIA_AUDIO"
            ),
            "SENSORS"  to setOf("android.permission.BODY_SENSORS", "android.permission.ACTIVITY_RECOGNITION")
        )
    }
}
