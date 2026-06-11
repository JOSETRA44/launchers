package com.tien.tensor.domain.model

enum class NetworkType { WIFI, MOBILE, NONE }

data class SystemStatus(
    val batteryPercent: Int = 0,
    val isCharging: Boolean = false,
    val networkType: NetworkType = NetworkType.NONE
)
