package com.tien.tensor.domain.model

data class AppUsageStat(
    val packageName: String,
    val appName: String,
    val totalTimeMs: Long
)
