package com.tien.tensor.presentation.arsenal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tien.tensor.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.tensor.di.AppModule
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.model.Severity
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalPromptHeader
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

/**
 * Security Arsenal hub: one card per registered plugin with its live
 * severity badge; tapping a card opens the streaming findings panel.
 */
@Composable
fun ArsenalScreen(
    onNavigateBack: () -> Unit,
    viewModel: ArsenalViewModel = viewModel(factory = AppModule.arsenalViewModelFactory())
) {
    val state  by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LauncherTheme.colors

    // The ViewModel outlives this screen (activity scope): start the plugin
    // jobs on entry and kill them on exit so nothing streams in the background.
    DisposableEffect(viewModel) {
        viewModel.onScreenEnter()
        onDispose { viewModel.onScreenExit() }
    }

    // System back mirrors the [BACK] button of the detail panel: while a
    // module is open it closes the panel; only from the hub does it leave
    // the screen (handled by the activity-level BackHandler).
    BackHandler(enabled = state.selectedModuleId != null) {
        viewModel.onCloseDetail()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
            .padding(horizontal = TensorSpacing.screenH)
    ) {
        Spacer(Modifier.height(TensorSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TerminalPromptHeader(path = "arsenal")
            TerminalButton(label = stringResource(R.string.common_back), onClick = onNavigateBack)
        }

        Spacer(Modifier.height(TensorSpacing.xs))
        Text(
            text  = stringResource(R.string.arsenal_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface
        )

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        val selected = state.selectedModule
        if (selected != null) {
            ArsenalDetailPanel(
                meta     = selected,
                report   = state.selectedReport,
                scanning = selected.id in state.scanningIds,
                onClose  = viewModel::onCloseDetail,
                onRescan = { viewModel.rescan(selected.id) }
            )
        } else {
            state.modules.forEach { meta ->
                ModuleCard(
                    meta     = meta,
                    report   = state.reports[meta.id],
                    scanning = meta.id in state.scanningIds,
                    onClick  = { viewModel.onSelectModule(meta.id) }
                )
                Spacer(Modifier.height(TensorSpacing.sm))
            }
        }
    }
}

@Composable
private fun ModuleCard(
    meta: ModuleMeta,
    report: ModuleReport?,
    scanning: Boolean,
    onClick: () -> Unit
) {
    val colors      = LauncherTheme.colors
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border)
            .background(colors.surface)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(TensorSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "▸ ${meta.name}", style = MaterialTheme.typography.titleSmall, color = colors.primary)
                if (meta.isStreaming) {
                    Spacer(Modifier.padding(start = TensorSpacing.xs))
                    Text(text = stringResource(R.string.arsenal_live), style = MaterialTheme.typography.labelSmall, color = colors.cursor)
                }
            }
            ModuleBadge(report = report, scanning = scanning)
        }
        Spacer(Modifier.height(TensorSpacing.xxs))
        Text(text = meta.tagline, style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
        if (report != null) {
            Spacer(Modifier.height(TensorSpacing.xxs))
            Text(text = report.headline, style = MaterialTheme.typography.labelSmall, color = colors.terminalPrompt)
        }
    }
}

@Composable
private fun ModuleBadge(report: ModuleReport?, scanning: Boolean) {
    val colors = LauncherTheme.colors
    val worst  = report?.worstSeverity
    val (label, color) = when {
        scanning && report == null               -> stringResource(R.string.arsenal_badge_scanning) to colors.onBackground
        report == null                           -> "[--]" to colors.onSurface
        worst == null || worst == Severity.INFO  -> stringResource(R.string.arsenal_badge_clean) to colors.primary
        else                                     -> "[${worst.name}]" to severityColor(worst)
    }
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
}
