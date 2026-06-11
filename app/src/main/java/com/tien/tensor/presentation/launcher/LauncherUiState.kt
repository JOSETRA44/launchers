package com.tien.tensor.presentation.launcher

import com.tien.tensor.domain.model.AppFolder
import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.domain.model.PinnedApp
import com.tien.tensor.domain.model.SmartApp
import com.tien.tensor.domain.model.SystemStatus

data class LauncherUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppInfo> = emptyList(),
    val smartApps: List<SmartApp> = emptyList(),
    val pinnedApps: List<PinnedApp> = emptyList(),
    val folders: List<AppFolder> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<AppInfo> = emptyList(),
    val currentTime: String = "--:--:--",
    val currentDate: String = "----.--.-- ---",
    val systemStatus: SystemStatus = SystemStatus(),
    val notificationCounts: Map<String, Int> = emptyMap(),
    val launchingAppName: String? = null,
    val commandOutput: String? = null,
    val showHelp: Boolean = false,
    val commandHistory: List<String> = emptyList(),
    val activeFolderId: String? = null,
    val showChargingOverlay: Boolean = false
)
