package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.PinnedApp
import com.tien.tensor.domain.port.PinnedAppsRepository
import kotlinx.coroutines.flow.Flow

class GetPinnedAppsUseCase(private val repository: PinnedAppsRepository) {
    operator fun invoke(): Flow<List<PinnedApp>> = repository.getPinnedApps()
}
