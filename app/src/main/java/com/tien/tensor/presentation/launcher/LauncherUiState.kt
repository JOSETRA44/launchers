package com.tien.tensor.presentation.launcher

import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.domain.model.SmartApp

data class LauncherUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppInfo> = emptyList(),
    /** Top apps by decay-frequency score. Falls back to first 5 alphabetical if empty. */
    val smartApps: List<SmartApp> = emptyList(),
    val searchQuery: String = "",
    /** Smart-sorted results: recently-used matches float to top. */
    val searchResults: List<AppInfo> = emptyList(),
    val currentTime: String = "--:--:--",
    val currentDate: String = "----.--.-- ---",
    val launchingAppName: String? = null
)
