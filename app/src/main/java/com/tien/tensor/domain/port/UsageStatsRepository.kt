package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.AppUsageStat

interface UsageStatsRepository {
    fun hasPermission(): Boolean
    suspend fun getTodayUsage(): List<AppUsageStat>
}
