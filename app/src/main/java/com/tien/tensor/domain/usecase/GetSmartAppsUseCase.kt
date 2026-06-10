package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.SmartApp
import com.tien.tensor.domain.port.AppRepository
import com.tien.tensor.domain.port.AppUsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Returns the top [limit] most relevant apps, ranked by decay-frequency score.
 * Filters out packages no longer installed by cross-referencing the live app list.
 */
class GetSmartAppsUseCase(
    private val usageRepository: AppUsageRepository,
    private val appRepository: AppRepository
) {
    operator fun invoke(limit: Int = 5): Flow<List<SmartApp>> =
        combine(
            usageRepository.getUsageStats(),
            appRepository.getInstalledApps()
        ) { stats, apps ->
            val nameMap = apps.associateBy { it.packageName }
            stats
                .sortedByDescending { it.score }
                .take(limit)
                .mapNotNull { stat ->
                    val appName = nameMap[stat.packageName]?.appName ?: return@mapNotNull null
                    SmartApp(
                        packageName  = stat.packageName,
                        appName      = appName,
                        launchCount  = stat.launchCount,
                        lastLaunchAt = stat.lastLaunchAt,
                        score        = stat.score
                    )
                }
        }
}
