package com.tien.tensor.domain.model

/**
 * Connectivity classification shown in the dynamic status bar.
 * Cellular generations require the (optional) phone-state permission;
 * without it the data layer degrades to the generic [CELLULAR].
 */
enum class NetworkType(val label: String) {
    WIFI("WIFI"),
    CELL_5G("5G"),
    CELL_4G("4G"),
    CELL_3G("3G"),
    CELL_2G("2G"),
    CELLULAR("CELL"),
    ETHERNET("ETH"),
    NONE("OFFLINE")
}

data class SystemStatus(
    val batteryPercent: Int = 0,
    val isCharging: Boolean = false,
    val isPowerSave: Boolean = false,
    val networkType: NetworkType = NetworkType.NONE,
    /** Signal strength 0..4 for the active transport; -1 when unknown. */
    val signalLevel: Int = -1
)
