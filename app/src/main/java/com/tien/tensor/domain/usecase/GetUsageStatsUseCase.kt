package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.AppUsageStat
import com.tien.tensor.domain.port.UsageStatsRepository

class GetUsageStatsUseCase(private val usageStatsRepository: UsageStatsRepository) {
    fun hasPermission(): Boolean = usageStatsRepository.hasPermission()
    suspend operator fun invoke(): List<AppUsageStat> = usageStatsRepository.getTodayUsage()
}
