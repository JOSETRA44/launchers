package com.tien.tensor.presentation.arsenal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tien.tensor.domain.model.Finding
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.ui.component.BlinkingCursor
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

/** Streaming findings panel for a single arsenal module. */
@Composable
fun ArsenalDetailPanel(
    meta: ModuleMeta,
    report: ModuleReport?,
    scanning: Boolean,
    onClose: () -> Unit,
    onRescan: () -> Unit
) {
    val colors = LauncherTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TerminalButton(label = "< MODULES", onClick = onClose)
        if (meta.isStreaming) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "LIVE ", style = MaterialTheme.typography.labelSmall, color = colors.cursor)
                BlinkingCursor()
            }
        } else {
            TerminalButton(label = if (scanning) "SCANNING.." else "RESCAN", onClick = onRescan)
        }
    }

    Spacer(Modifier.height(TensorSpacing.sm))
    Text(text = "▸ ${meta.name}", style = MaterialTheme.typography.titleMedium, color = colors.primary)
    Text(text = meta.tagline, style = MaterialTheme.typography.labelSmall, color = colors.onSurface)

    if (report != null) {
        Spacer(Modifier.height(TensorSpacing.xs))
        Text(text = report.headline, style = MaterialTheme.typography.labelMedium, color = colors.terminalPrompt)
    }

    TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

    when {
        report == null && scanning ->
            Text(text = "> running analysis...", style = MaterialTheme.typography.bodySmall, color = colors.onBackground)
        report == null ->
            Text(text = "> no data.", style = MaterialTheme.typography.bodySmall, color = colors.onSurface)
        else ->
            LazyColumn {
                items(report.findings, key = { it.id }) { finding ->
                    FindingRow(finding)
                    Spacer(Modifier.height(TensorSpacing.sm))
                }
            }
    }
}

@Composable
private fun FindingRow(finding: Finding) {
    val colors = LauncherTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = "[${finding.severity.name}]",
                style = MaterialTheme.typography.labelSmall,
                color = severityColor(finding.severity)
            )
            Spacer(Modifier.padding(start = TensorSpacing.xs))
            Text(text = finding.title, style = MaterialTheme.typography.bodyMedium, color = colors.onBackground)
        }
        Text(
            text     = finding.detail,
            style    = MaterialTheme.typography.labelSmall,
            color    = colors.onSurface,
            modifier = Modifier.padding(start = TensorSpacing.md)
        )
    }
}

@Composable
internal fun severityColor(severity: Severity): Color {
    val colors = LauncherTheme.colors
    return when (severity) {
        Severity.CRITICAL, Severity.HIGH -> colors.error
        Severity.MEDIUM                  -> colors.cursor
        Severity.LOW                     -> colors.terminalPrompt
        Severity.INFO                    -> colors.onSurface
    }
}
