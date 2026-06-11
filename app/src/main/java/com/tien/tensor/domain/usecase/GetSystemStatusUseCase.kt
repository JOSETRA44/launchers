package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.SystemStatus
import com.tien.tensor.domain.port.SystemStatusRepository
import kotlinx.coroutines.flow.Flow

class GetSystemStatusUseCase(private val repository: SystemStatusRepository) {
    operator fun invoke(): Flow<SystemStatus> = repository.getSystemStatus()
}
