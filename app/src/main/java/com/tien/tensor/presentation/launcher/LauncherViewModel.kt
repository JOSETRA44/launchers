package com.tien.tensor.presentation.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.usecase.GetInstalledAppsUseCase
import com.tien.tensor.domain.usecase.LaunchAppUseCase
import com.tien.tensor.domain.usecase.SearchAppsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LauncherViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val searchAppsUseCase: SearchAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase
) : ViewModel() {

    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val dateFmt = SimpleDateFormat("yyyy.MM.dd | EEE", Locale.US)

    private val _searchQuery     = MutableStateFlow("")
    private val _currentTime     = MutableStateFlow(formattedTime())
    private val _currentDate     = MutableStateFlow(formattedDate())
    private val _launchingAppName = MutableStateFlow<String?>(null)

    private val apps = getInstalledAppsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = combine(
        apps, _searchQuery, _currentTime, _currentDate, _launchingAppName
    ) { appList, query, time, date, launching ->
        LauncherUiState(
            isLoading        = false,
            allApps          = appList,
            quickAccessApps  = appList.take(8),
            searchQuery      = query,
            searchResults    = searchAppsUseCase(query, appList),
            currentTime      = time,
            currentDate      = date.uppercase(),
            launchingAppName = launching
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherUiState())

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _currentTime.value = formattedTime()
                _currentDate.value = formattedDate()
            }
        }
    }

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }

    fun onAppLaunch(packageName: String, appName: String) {
        viewModelScope.launch {
            _launchingAppName.value = appName
            launchAppUseCase(packageName)
            delay(800)
            _launchingAppName.value = null
        }
    }

    private fun formattedTime(): String = timeFmt.format(Calendar.getInstance().time)
    private fun formattedDate(): String = dateFmt.format(Calendar.getInstance().time)
}
