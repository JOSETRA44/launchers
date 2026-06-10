package com.tien.tensor.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.usecase.GetThemeUseCase
import com.tien.tensor.domain.usecase.SetThemeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase
) : ViewModel() {

    val uiState = getThemeUseCase()
        .map { themeId -> SettingsUiState(selectedThemeId = themeId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onThemeSelected(themeId: ThemeId) {
        viewModelScope.launch { setThemeUseCase(themeId) }
    }
}
