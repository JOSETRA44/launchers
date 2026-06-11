package com.tien.tensor.data.repository

import com.tien.tensor.data.source.SystemStatusDataSource
import com.tien.tensor.domain.model.SystemStatus
import com.tien.tensor.domain.port.SystemStatusRepository
import kotlinx.coroutines.flow.Flow

class SystemStatusRepositoryImpl(
    private val dataSource: SystemStatusDataSource
) : SystemStatusRepository {
    override fun getSystemStatus(): Flow<SystemStatus> = dataSource.systemStatus
}
