package com.tien.tensor.data.arsenal

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
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
 * Audits which apps hold device-wide access powers — the classic spyware
 * surface. Read-only enumeration of:
 *
 * - Enabled accessibility services (can read the screen and inject input)
 * - Enabled notification listeners (can read every notification)
 * - Active device administrators (can lock/wipe policies)
 * - The default keyboard (sees everything typed)
 */
class AccessControlModule(private val context: Context) : SecurityModule {

    override val meta = ModuleMeta(
        id          = "access_control",
        name        = "ACCESS CONTROL",
        tagline     = "Accessibility · notif listeners · admins · IME",
        isStreaming = false
    )

    override fun observe(): Flow<ModuleReport> =
        flow { emit(scan()) }.flowOn(Dispatchers.IO)

    private fun scan(): ModuleReport {
        val pm       = context.packageManager
        val findings = mutableListOf<Finding>()

        // ── Accessibility services — strongest surveillance capability ───────
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val a11y = am.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        if (a11y.isEmpty()) {
            findings += Finding("ac_a11y", "ACCESSIBILITY SERVICES", "None enabled.", Severity.INFO)
        } else {
            a11y.forEach { info ->
                val pkg   = info.resolveInfo.serviceInfo.packageName
                val label = try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() } catch (e: Exception) { pkg }
                findings += Finding(
                    "ac_a11y_$pkg", "ACCESSIBILITY: $label",
                    "$pkg can read the screen and inject input on every app. Verify you trust it.",
                    Severity.HIGH
                )
            }
        }

        // ── Notification listeners ────────────────────────────────────────────
        val listeners = NotificationManagerCompat.getEnabledListenerPackages(context)
            .filter { it != context.packageName }
        if (listeners.isEmpty()) {
            findings += Finding("ac_notif", "NOTIFICATION LISTENERS", "No third-party listeners.", Severity.INFO)
        } else {
            listeners.forEach { pkg ->
                val label = try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() } catch (e: Exception) { pkg }
                findings += Finding(
                    "ac_notif_$pkg", "NOTIF LISTENER: $label",
                    "$pkg reads every notification, including 2FA codes and private messages.",
                    Severity.MEDIUM
                )
            }
        }

        // ── Device administrators ─────────────────────────────────────────────
        val dpm    = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admins = dpm.activeAdmins ?: emptyList()
        if (admins.isEmpty()) {
            findings += Finding("ac_admin", "DEVICE ADMINS", "No active device administrators.", Severity.INFO)
        } else {
            admins.forEach { cn ->
                val label = try { pm.getApplicationLabel(pm.getApplicationInfo(cn.packageName, 0)).toString() } catch (e: Exception) { cn.packageName }
                findings += Finding(
                    "ac_admin_${cn.packageName}", "DEVICE ADMIN: $label",
                    "${cn.packageName} holds device-policy powers (lock, password rules, possibly wipe).",
                    Severity.MEDIUM
                )
            }
        }

        // ── Default keyboard ──────────────────────────────────────────────────
        val ime = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        if (!ime.isNullOrBlank()) {
            val imePkg = ime.substringBefore('/')
            findings += Finding(
                "ac_ime", "ACTIVE KEYBOARD",
                "$imePkg processes everything you type, including passwords.",
                Severity.INFO
            )
        }

        return ModuleReport(
            moduleId  = meta.id,
            headline  = "${a11y.size} A11Y · ${listeners.size} LISTENERS · ${admins.size} ADMINS",
            findings  = findings.sortedByDescending { it.severity.ordinal },
            updatedAt = System.currentTimeMillis()
        )
    }
}
