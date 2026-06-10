package com.tien.tensor.presentation.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.domain.model.SmartApp
import com.tien.tensor.domain.usecase.GetInstalledAppsUseCase
import com.tien.tensor.domain.usecase.GetSmartAppsUseCase
import com.tien.tensor.domain.usecase.LaunchAppUseCase
import com.tien.tensor.domain.usecase.SearchAppsUseCase
import com.tien.tensor.domain.usecase.TrackAppLaunchUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LauncherViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val getSmartAppsUseCase: GetSmartAppsUseCase,
    private val searchAppsUseCase: SearchAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val trackAppLaunchUseCase: TrackAppLaunchUseCase
) : ViewModel() {

    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val dateFmt = SimpleDateFormat("yyyy.MM.dd | EEE", Locale.US)

    private val _state = MutableStateFlow(
        LauncherUiState(currentTime = formattedTime(), currentDate = formattedDate())
    )
    val uiState: StateFlow<LauncherUiState> = _state.asStateFlow()

    init {
        // Live installed app list
        viewModelScope.launch {
            getInstalledAppsUseCase().collect { apps ->
                _state.update { s ->
                    s.copy(
                        isLoading     = false,
                        allApps       = apps,
                        searchResults = smartSearch(s.searchQuery, apps, s.smartApps)
                    )
                }
            }
        }
        // Smart recent apps (re-emits after each tracked launch)
        viewModelScope.launch {
            getSmartAppsUseCase().collect { smart ->
                _state.update { s ->
                    s.copy(
                        smartApps     = smart,
                        searchResults = smartSearch(s.searchQuery, s.allApps, smart)
                    )
                }
            }
        }
        // 1-second clock tick
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _state.update { it.copy(currentTime = formattedTime(), currentDate = formattedDate()) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { s ->
            s.copy(
                searchQuery   = query,
                searchResults = smartSearch(query, s.allApps, s.smartApps)
            )
        }
    }

    fun onAppLaunch(packageName: String, appName: String) {
        viewModelScope.launch {
            _state.update { it.copy(launchingAppName = appName) }
            launchAppUseCase(packageName)
            trackAppLaunchUseCase(packageName)
            delay(700)
            _state.update { it.copy(launchingAppName = null) }
        }
    }

    /**
     * Smart search: exact-prefix matches first, then recently-used apps that
     * match float above the rest, then alphabetical order.
     */
    private fun smartSearch(
        query: String,
        apps: List<AppInfo>,
        smart: List<SmartApp>
    ): List<AppInfo> {
        val base = searchAppsUseCase(query, apps)
        if (query.isBlank()) return base
        val q = query.trim().lowercase()
        val recentPkgs = smart.map { it.packageName }.toHashSet()
        return base.sortedWith(
            compareByDescending<AppInfo> { it.appName.lowercase().startsWith(q) }
                .thenByDescending { it.packageName in recentPkgs }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun formattedTime() = timeFmt.format(Calendar.getInstance().time)
    private fun formattedDate() = dateFmt.format(Calendar.getInstance().time).uppercase()
}
