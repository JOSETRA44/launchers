package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.UiPrefs
import com.tien.tensor.domain.port.UiPrefsRepository
import kotlinx.coroutines.flow.Flow

class GetUiPrefsUseCase(private val repository: UiPrefsRepository) {
    operator fun invoke(): Flow<UiPrefs> = repository.getPrefs()
}
