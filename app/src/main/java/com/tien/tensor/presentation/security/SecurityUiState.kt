package com.tien.tensor.presentation.security

import com.tien.tensor.domain.model.SecurityReport

data class SecurityUiState(
    val isLoading: Boolean = true,
    val report: SecurityReport = SecurityReport(),
    val passwordLength: Int = 16,
    val generatedPassword: String = "",
    val hashInput: String = "",
    val hashOutput: String = ""
)
