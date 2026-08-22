package com.tien.tensor.presentation.arsenal

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.tien.tensor.R
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
        TerminalButton(label = stringResource(R.string.arsenal_modules_back), onClick = onClose)
        if (meta.isStreaming) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.arsenal_live) + " ", style = MaterialTheme.typography.labelSmall, color = colors.cursor)
                BlinkingCursor()
            }
        } else {
            TerminalButton(label = if (scanning) stringResource(R.string.arsenal_scanning) else stringResource(R.string.common_rescan), onClick = onRescan)
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

    PermissionGate(meta, onRescan)

    when {
        report == null && scanning ->
            Text(text = stringResource(R.string.arsenal_running), style = MaterialTheme.typography.bodySmall, color = colors.onBackground)
        report == null ->
            Text(text = stringResource(R.string.arsenal_no_data), style = MaterialTheme.typography.bodySmall, color = colors.onSurface)
        else ->
            LazyColumn {
                items(report.findings, key = { it.id }) { finding ->
                    FindingRow(finding)
                    Spacer(Modifier.height(TensorSpacing.sm))
                }
            }
    }
}

/**
 * One-tap runtime-permission request for modules that declare
 * [ModuleMeta.requiredPermissions]. Renders nothing when every permission is
 * already granted; otherwise shows why the module is degraded and a button
 * that requests the missing grants, re-running the scan on return.
 */
@Composable
private fun PermissionGate(meta: ModuleMeta, onRescan: () -> Unit) {
    if (meta.requiredPermissions.isEmpty()) return
    val colors = LauncherTheme.colors
    val context = LocalContext.current
    var recheck by remember { mutableIntStateOf(0) }

    val missing = remember(meta, recheck) {
        meta.requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        recheck++
        onRescan()
    }

    if (missing.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = TensorSpacing.sm)) {
        Text(
            text  = stringResource(R.string.arsenal_perm_required),
            style = MaterialTheme.typography.labelSmall,
            color = colors.cursor
        )
        Spacer(Modifier.height(TensorSpacing.xs))
        TerminalButton(
            label   = stringResource(R.string.arsenal_perm_grant),
            onClick = { launcher.launch(missing.toTypedArray()) }
        )
        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))
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
