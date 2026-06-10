package com.tien.tensor.presentation.applist

import com.tien.tensor.domain.model.AppInfo

data class AppListUiState(
    val isLoading: Boolean = true,
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = ""
)
