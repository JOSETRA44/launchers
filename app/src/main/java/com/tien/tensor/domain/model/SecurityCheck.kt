package com.tien.tensor.domain.model

enum class CheckStatus { PASS, WARN, INFO }

data class SecurityCheck(
    val id: String,
    val label: String,
    val status: CheckStatus,
    val detail: String
)

data class SecurityReport(val checks: List<SecurityCheck> = emptyList()) {
    val warningCount: Int get() = checks.count { it.status == CheckStatus.WARN }
}
