package com.tien.tensor.data.repository

import com.tien.tensor.data.source.SecurityDataSource
import com.tien.tensor.domain.model.SecurityReport
import com.tien.tensor.domain.port.SecurityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecurityRepositoryImpl(
    private val securityDataSource: SecurityDataSource
) : SecurityRepository {

    override suspend fun getReport(): SecurityReport = withContext(Dispatchers.IO) {
        SecurityReport(checks = securityDataSource.runChecks())
    }
}
