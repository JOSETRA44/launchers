package com.tien.tensor.data.arsenal

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import com.tien.tensor.domain.model.Finding
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.domain.port.SecurityModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.util.Locale

/**
 * Live hardware/runtime telemetry: memory pressure, storage headroom,
 * battery thermals and the system thermal-throttling state. Emits a fresh
 * snapshot every [INTERVAL_MS] while observed; sampling stops the moment
 * the collector leaves the screen.
 */
class RuntimeTelemetryModule(private val context: Context) : SecurityModule {

    override val meta = ModuleMeta(
        id          = "telemetry",
        name        = "SYS TELEMETRY",
        tagline     = "Memory · storage · thermals — live",
        isStreaming = true
    )

    override fun observe(): Flow<ModuleReport> = flow {
        while (currentCoroutineContext().isActive) {
            emit(snapshot())
            delay(INTERVAL_MS)
        }
    }.flowOn(Dispatchers.IO)

    private fun snapshot(): ModuleReport {
        val findings = mutableListOf<Finding>()

        // ── Memory ────────────────────────────────────────────────────────────
        val am  = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val usedPct = ((mem.totalMem - mem.availMem) * 100 / mem.totalMem).toInt()
        findings += Finding(
            "tel_ram", "MEMORY",
            "${gb(mem.totalMem - mem.availMem)} / ${gb(mem.totalMem)} GB in use ($usedPct%)",
            when {
                mem.lowMemory  -> Severity.HIGH
                usedPct >= 90  -> Severity.MEDIUM
                else           -> Severity.INFO
            }
        )

        // ── Storage ───────────────────────────────────────────────────────────
        val stat    = StatFs(Environment.getDataDirectory().path)
        val freePct = (stat.availableBytes * 100 / stat.totalBytes).toInt()
        findings += Finding(
            "tel_disk", "STORAGE",
            "${gb(stat.availableBytes)} GB free of ${gb(stat.totalBytes)} GB ($freePct%)",
            if (freePct < 10) Severity.MEDIUM else Severity.INFO
        )

        // ── Battery thermals ──────────────────────────────────────────────────
        val batt    = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val tempC   = (batt?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1) / 10f
        val voltage = batt?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        if (tempC > 0) {
            findings += Finding(
                "tel_batt", "BATTERY",
                String.format(Locale.US, "%.1f°C · %d mV", tempC, voltage),
                when {
                    tempC >= 45f -> Severity.HIGH
                    tempC >= 40f -> Severity.MEDIUM
                    else         -> Severity.INFO
                }
            )
        }

        // ── Thermal throttling ────────────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val (label, severity) = when (pm.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE     -> "nominal" to Severity.INFO
                PowerManager.THERMAL_STATUS_LIGHT    -> "light throttling" to Severity.INFO
                PowerManager.THERMAL_STATUS_MODERATE -> "moderate throttling" to Severity.MEDIUM
                else                                 -> "severe throttling" to Severity.HIGH
            }
            findings += Finding("tel_thermal", "THERMAL STATE", "System reports $label.", severity)
        }

        // ── Uptime ────────────────────────────────────────────────────────────
        val upMin = SystemClock.elapsedRealtime() / 60_000
        findings += Finding("tel_uptime", "UPTIME", "${upMin / 60}h ${upMin % 60}m since boot", Severity.INFO)

        return ModuleReport(
            moduleId  = meta.id,
            headline  = String.format(Locale.US, "RAM %d%% · %s GB FREE · %.1f°C", usedPct, gb(stat.availableBytes), tempC),
            findings  = findings,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun gb(bytes: Long): String = String.format(Locale.US, "%.1f", bytes / 1_073_741_824.0)

    private companion object { const val INTERVAL_MS = 2_500L }
}
