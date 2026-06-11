package com.tien.tensor.domain.model

/**
 * Security Arsenal domain model. Every tool in the arsenal is a plugin
 * (see `domain/port/SecurityModule`) that emits [ModuleReport]s.
 * All modules are read-only, defensive audits of the user's own device.
 */

enum class Severity { INFO, LOW, MEDIUM, HIGH, CRITICAL }

data class Finding(
    val id: String,
    val title: String,
    val detail: String,
    val severity: Severity
)

/** Static identity of a plugin, shown on the arsenal hub. */
data class ModuleMeta(
    val id: String,
    val name: String,
    val tagline: String,
    /** True when the module emits continuously (live telemetry) instead of one-shot scans. */
    val isStreaming: Boolean
)

data class ModuleReport(
    val moduleId: String,
    /** One-line machine-style summary, e.g. "142 PKGS ANALYZED · 3 FLAGGED". */
    val headline: String,
    val findings: List<Finding>,
    val updatedAt: Long
) {
    val worstSeverity: Severity? = findings.maxByOrNull { it.severity.ordinal }?.severity
}
