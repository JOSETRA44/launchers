package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.PinnedAppsRepository

class PinAppUseCase(private val repository: PinnedAppsRepository) {
    suspend operator fun invoke(packageName: String, appName: String): Boolean =
        repository.pinApp(packageName, appName)
}
