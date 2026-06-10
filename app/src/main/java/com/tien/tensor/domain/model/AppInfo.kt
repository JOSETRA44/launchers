package com.tien.tensor.domain.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false
)
