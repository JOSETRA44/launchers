package com.tien.tensor.presentation.insights

import com.tien.tensor.domain.model.AppUsageStat
import com.tien.tensor.domain.model.StepData

data class InsightsUiState(
    val isLoading: Boolean = true,
    val hasUsagePermission: Boolean = false,
    val usageStats: List<AppUsageStat> = emptyList(),
    val totalScreenTimeMs: Long = 0L,
    val steps: StepData = StepData()
)
