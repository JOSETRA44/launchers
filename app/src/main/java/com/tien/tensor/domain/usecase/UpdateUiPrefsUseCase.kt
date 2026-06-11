package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.UiPrefs
import com.tien.tensor.domain.port.UiPrefsRepository

class UpdateUiPrefsUseCase(private val repository: UiPrefsRepository) {
    suspend operator fun invoke(prefs: UiPrefs) = repository.update(prefs)
}
