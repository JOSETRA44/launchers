package com.tien.tensor.data.repository

import com.tien.tensor.data.source.AppUsageDataSource
import com.tien.tensor.domain.model.AppUsageStats
import com.tien.tensor.domain.port.AppUsageRepository
import kotlinx.coroutines.flow.Flow

class AppUsageRepositoryImpl(
    private val dataSource: AppUsageDataSource
) : AppUsageRepository {
    override fun getUsageStats(): Flow<List<AppUsageStats>> = dataSource.usageStats
    override suspend fun trackLaunch(packageName: String) = dataSource.trackLaunch(packageName)
    override suspend fun clearAllHistory() = dataSource.clearAllHistory()
}
