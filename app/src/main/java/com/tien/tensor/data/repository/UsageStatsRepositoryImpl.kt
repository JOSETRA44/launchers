package com.tien.tensor.data.repository

import com.tien.tensor.data.source.UsageStatsDataSource
import com.tien.tensor.domain.model.AppUsageStat
import com.tien.tensor.domain.port.UsageStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageStatsRepositoryImpl(
    private val usageStatsDataSource: UsageStatsDataSource
) : UsageStatsRepository {

    override fun hasPermission(): Boolean = usageStatsDataSource.hasPermission()

    override suspend fun getTodayUsage(): List<AppUsageStat> = withContext(Dispatchers.IO) {
        usageStatsDataSource.getTodayUsage()
    }
}
