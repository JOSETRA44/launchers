package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.domain.port.AppRepository
import kotlinx.coroutines.flow.Flow

class GetInstalledAppsUseCase(private val repository: AppRepository) {
    operator fun invoke(): Flow<List<AppInfo>> = repository.getInstalledApps()
}
