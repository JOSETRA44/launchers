package com.tien.tensor.presentation.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.usecase.GetInstalledAppsUseCase
import com.tien.tensor.domain.usecase.LaunchAppUseCase
import com.tien.tensor.domain.usecase.SearchAppsUseCase
import com.tien.tensor.domain.usecase.TrackAppLaunchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppListViewModel(
    getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val searchAppsUseCase: SearchAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val trackAppLaunchUseCase: TrackAppLaunchUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val apps = getInstalledAppsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = combine(apps, _searchQuery) { appList, query ->
        AppListUiState(
            isLoading    = false,
            apps         = appList,
            filteredApps = searchAppsUseCase(query, appList),
            searchQuery  = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppListUiState())

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }

    fun onAppLaunch(packageName: String) {
        viewModelScope.launch {
            launchAppUseCase(packageName)
            trackAppLaunchUseCase(packageName)
        }
    }
}
