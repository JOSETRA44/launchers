package com.tien.tensor.domain.model

/** Aggregated usage data for one app, computed by the data layer. */
data class AppUsageStats(
    val packageName: String,
    val launchCount: Int,
    val lastLaunchAt: Long,
    /** Decay-frequency score: Σ 1/(1 + age_hours/24) — higher is better. */
    val score: Double
)
