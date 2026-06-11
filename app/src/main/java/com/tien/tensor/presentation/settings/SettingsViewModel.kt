package com.tien.tensor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.model.BarSize
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.model.UiPrefs
import com.tien.tensor.domain.usecase.ClearHistoryUseCase
import com.tien.tensor.domain.usecase.GetThemeUseCase
import com.tien.tensor.domain.usecase.GetUiPrefsUseCase
import com.tien.tensor.domain.usecase.SetThemeUseCase
import com.tien.tensor.domain.usecase.UpdateUiPrefsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    getThemeUseCase: GetThemeUseCase,
    getUiPrefsUseCase: GetUiPrefsUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val updateUiPrefsUseCase: UpdateUiPrefsUseCase,
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
        viewModelScope.launch {
            getUiPrefsUseCase().collect { prefs ->
                _state.update { it.copy(uiPrefs = prefs) }
            }
        }
    }

    fun onThemeSelected(themeId: ThemeId) {
        viewModelScope.launch { setThemeUseCase(themeId) }
    }

    // ── UI customization ──────────────────────────────────────────────────────

    fun onBarSizeSelected(size: BarSize)   = updatePrefs { it.copy(statusBarSize = size) }
    fun onFontScaleSelected(scale: Float)  = updatePrefs { it.copy(fontScale = scale) }
    fun onClockFormatSelected(use24h: Boolean) = updatePrefs { it.copy(use24hClock = use24h) }
    fun onToggleClockSeconds()             = updatePrefs { it.copy(showClockSeconds = !it.showClockSeconds) }
    fun onLanguageSelected(tag: String)    = updatePrefs { it.copy(language = tag) }

    /** Steps a physical-edge safety margin by ±[UiPrefs.MARGIN_STEP_DP]; applied live. */
    fun onMarginStepped(top: Boolean, increase: Boolean) = updatePrefs {
        val delta = if (increase) UiPrefs.MARGIN_STEP_DP else -UiPrefs.MARGIN_STEP_DP
        if (top) it.copy(marginTopDp = (it.marginTopDp + delta).coerceIn(0, UiPrefs.MARGIN_MAX_DP))
        else     it.copy(marginBottomDp = (it.marginBottomDp + delta).coerceIn(0, UiPrefs.MARGIN_MAX_DP))
    }

    private fun updatePrefs(transform: (UiPrefs) -> UiPrefs) {
        viewModelScope.launch { updateUiPrefsUseCase(transform(_state.value.uiPrefs)) }
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
