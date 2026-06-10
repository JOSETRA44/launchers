package com.tien.tensor.presentation.settings

import com.tien.tensor.domain.model.ThemeConfig
import com.tien.tensor.domain.model.ThemeId

data class SettingsUiState(
    val selectedThemeId: ThemeId = ThemeId.HACKER_DARK,
    val availableThemes: List<ThemeConfig> = ThemeId.entries.map { ThemeConfig(it) }
)
