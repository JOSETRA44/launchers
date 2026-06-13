package com.tien.tensor.presentation.settings

import com.tien.tensor.domain.model.ThemeConfig
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.model.UiPrefs

data class SettingsUiState(
    val selectedThemeId: ThemeId = ThemeId.HACKER_DARK,
    val availableThemes: List<ThemeConfig> = ThemeId.entries.map { ThemeConfig(it) },
    val uiPrefs: UiPrefs = UiPrefs(),
    val historyCleared: Boolean = false,
    /** Absolute path of the imported wallpaper image, or null when unset. */
    val wallpaperPath: String? = null
)
