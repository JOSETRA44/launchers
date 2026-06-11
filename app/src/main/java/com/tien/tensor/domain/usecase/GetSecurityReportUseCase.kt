package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.SecurityReport
import com.tien.tensor.domain.port.SecurityRepository

class GetSecurityReportUseCase(private val securityRepository: SecurityRepository) {
    suspend operator fun invoke(): SecurityReport = securityRepository.getReport()
}
