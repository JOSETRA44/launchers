package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.port.ThemeRepository
import kotlinx.coroutines.flow.Flow

class GetThemeUseCase(private val repository: ThemeRepository) {
    operator fun invoke(): Flow<ThemeId> = repository.getSelectedTheme()
}
