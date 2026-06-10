package com.tien.tensor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.usecase.ClearHistoryUseCase
import com.tien.tensor.domain.usecase.GetThemeUseCase
import com.tien.tensor.domain.usecase.SetThemeUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getThemeUseCase().collect { themeId ->
                _state.update { it.copy(selectedThemeId = themeId) }
            }
        }
    }

    fun onThemeSelected(themeId: ThemeId) {
        viewModelScope.launch { setThemeUseCase(themeId) }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            clearHistoryUseCase()
            _state.update { it.copy(historyCleared = true) }
            delay(2_000)
            _state.update { it.copy(historyCleared = false) }
        }
    }
}
