package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.SecurityReport

interface SecurityRepository {
    suspend fun getReport(): SecurityReport
}
