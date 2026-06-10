package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.port.ThemeRepository

class SetThemeUseCase(private val repository: ThemeRepository) {
    suspend operator fun invoke(themeId: ThemeId) = repository.setTheme(themeId)
}
