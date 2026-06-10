package com.tien.tensor.domain.model

/**
 * Enriched app entry for the RECENT / smart-access section.
 * Combines AppUsageStats with the human-readable app name from AppRepository.
 */
data class SmartApp(
    val packageName: String,
    val appName: String,
    val launchCount: Int,
    val lastLaunchAt: Long,
    val score: Double
)
