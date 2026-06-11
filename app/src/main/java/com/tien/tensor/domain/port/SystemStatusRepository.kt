package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.SystemStatus
import kotlinx.coroutines.flow.Flow

interface SystemStatusRepository {
    fun getSystemStatus(): Flow<SystemStatus>
}
