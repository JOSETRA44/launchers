package com.tien.tensor.presentation.launcher

import com.tien.tensor.domain.model.AppInfo

data class LauncherUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppInfo> = emptyList(),
    val quickAccessApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<AppInfo> = emptyList(),
    val currentTime: String = "--:--:--",
    val currentDate: String = "----.--.-- ---",
    val launchingAppName: String? = null
)
