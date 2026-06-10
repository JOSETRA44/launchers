package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.AppUsageRepository

class TrackAppLaunchUseCase(private val repository: AppUsageRepository) {
    suspend operator fun invoke(packageName: String) = repository.trackLaunch(packageName)
}
