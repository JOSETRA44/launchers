package com.tien.tensor.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.usecase.GetStepsUseCase
import com.tien.tensor.domain.usecase.GetUsageStatsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TOP_APPS_SHOWN = 10

class InsightsViewModel(
    private val getUsageStatsUseCase: GetUsageStatsUseCase,
    private val getStepsUseCase: GetStepsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _state.asStateFlow()

    private var stepsJob: Job? = null

    init {
        refreshUsage()
        startStepTracking()
    }

    /** Re-queried on every screen resume so a permission granted in Settings takes effect. */
    fun refreshUsage() {
        viewModelScope.launch {
            val granted = getUsageStatsUseCase.hasPermission()
            if (!granted) {
                _state.update { it.copy(isLoading = false, hasUsagePermission = false, usageStats = emptyList(), totalScreenTimeMs = 0) }
                return@launch
            }
            val stats = getUsageStatsUseCase()
            _state.update {
                it.copy(
                    isLoading          = false,
                    hasUsagePermission = true,
                    usageStats         = stats.take(TOP_APPS_SHOWN),
                    totalScreenTimeMs  = stats.sumOf { s -> s.totalTimeMs }
                )
            }
        }
    }

    /** Restartable: called again after ACTIVITY_RECOGNITION is granted so the sensor re-registers. */
    fun startStepTracking() {
        stepsJob?.cancel()
        stepsJob = viewModelScope.launch {
            getStepsUseCase().collect { data -> _state.update { it.copy(steps = data) } }
        }
    }
}
