package com.tien.tensor.data.source

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.tien.tensor.domain.model.AppInfo

class AppDataSource(private val packageManager: PackageManager) {

    fun queryInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        return resolveInfoList
            .map { info ->
                AppInfo(
                    packageName = info.activityInfo.packageName,
                    appName = info.loadLabel(packageManager).toString(),
                    isSystemApp = (info.activityInfo.applicationInfo.flags
                            and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }
}
