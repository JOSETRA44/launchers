package com.tien.tensor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.model.BarSize
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.model.UiPrefs
import com.tien.tensor.domain.model.WallpaperAnchor
import com.tien.tensor.domain.usecase.ClearHistoryUseCase
import com.tien.tensor.domain.usecase.ClearWallpaperUseCase
import com.tien.tensor.domain.usecase.GetThemeUseCase
import com.tien.tensor.domain.usecase.GetUiPrefsUseCase
import com.tien.tensor.domain.usecase.ObserveWallpaperUseCase
import com.tien.tensor.domain.usecase.SetThemeUseCase
import com.tien.tensor.domain.usecase.SetWallpaperUseCase
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
    private val clearHistoryUseCase: ClearHistoryUseCase,
    observeWallpaperUseCase: ObserveWallpaperUseCase,
    private val setWallpaperUseCase: SetWallpaperUseCase,
    private val clearWallpaperUseCase: ClearWallpaperUseCase
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
        viewModelScope.launch {
            observeWallpaperUseCase().collect { path ->
                _state.update { it.copy(wallpaperPath = path) }
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
    fun onShowDateToggled()                = updatePrefs { it.copy(showDate = !it.showDate) }
    fun onStatusBarOpaqueToggled()         = updatePrefs { it.copy(statusBarOpaque = !it.statusBarOpaque) }
    fun onCursorBlinkSelected(blink: Boolean) = updatePrefs { it.copy(cursorBlink = blink) }
    fun onTypingSpeedSelected(ms: Int)     = updatePrefs { it.copy(typingSpeedMs = ms) }

    /**
     * Steps a physical-edge margin by ±[UiPrefs.MARGIN_STEP_DP]; applied live.
     * The value is signed: below zero it compensates the system inset so the
     * UI can hug the hardware edge (see UiPrefs / MainActivity).
     */
    fun onMarginStepped(top: Boolean, increase: Boolean) = updatePrefs {
        val delta = if (increase) UiPrefs.MARGIN_STEP_DP else -UiPrefs.MARGIN_STEP_DP
        if (top) it.copy(marginTopDp = (it.marginTopDp + delta).coerceIn(UiPrefs.MARGIN_MIN_DP, UiPrefs.MARGIN_MAX_DP))
        else     it.copy(marginBottomDp = (it.marginBottomDp + delta).coerceIn(UiPrefs.MARGIN_MIN_DP, UiPrefs.MARGIN_MAX_DP))
    }

    // ── Wallpaper sticker ─────────────────────────────────────────────────────

    /** [uri] is the platform content-Uri as text; the data layer imports a copy. */
    fun onWallpaperPicked(uri: String)  { viewModelScope.launch { setWallpaperUseCase(uri) } }
    fun onWallpaperCleared()            { viewModelScope.launch { clearWallpaperUseCase() } }
    fun onWallpaperAlphaSelected(alpha: Float)        = updatePrefs { it.copy(wallpaperAlpha = alpha) }
    fun onWallpaperSizeSelected(sizePct: Int)         = updatePrefs { it.copy(wallpaperSizePct = sizePct) }
    fun onWallpaperAnchorSelected(anchor: WallpaperAnchor) = updatePrefs { it.copy(wallpaperAnchor = anchor) }

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
