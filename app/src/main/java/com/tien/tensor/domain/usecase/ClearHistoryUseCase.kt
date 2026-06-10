package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.AppUsageRepository

class ClearHistoryUseCase(private val repository: AppUsageRepository) {
    suspend operator fun invoke() = repository.clearAllHistory()
}
