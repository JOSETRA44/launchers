package com.tien.tensor.data.source

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import com.tien.tensor.domain.model.AppUsageStat
import java.util.Calendar

class UsageStatsDataSource(private val context: Context) {

    fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getTodayUsage(): List<AppUsageStat> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startOfDay, System.currentTimeMillis()
        ) ?: return emptyList()

        val pm = context.packageManager
        return stats.asSequence()
            // INTERVAL_DAILY can return overlapping buckets — keep the max per package
            .groupBy { it.packageName }
            .mapValues { (_, list) -> list.maxOf { it.totalTimeInForeground } }
            .filter { (pkg, time) -> time > 0 && pkg != context.packageName }
            .mapNotNull { (pkg, time) ->
                val label = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                }.getOrNull() ?: return@mapNotNull null
                AppUsageStat(packageName = pkg, appName = label, totalTimeMs = time)
            }
            .sortedByDescending { it.totalTimeMs }
            .toList()
    }
}
