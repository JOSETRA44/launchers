package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.PinnedAppsRepository

class UnpinAppUseCase(private val repository: PinnedAppsRepository) {
    suspend operator fun invoke(packageName: String) = repository.unpinApp(packageName)
}
