package com.tien.tensor.presentation.insights

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.tien.tensor.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.tensor.di.AppModule
import com.tien.tensor.domain.model.AppUsageStat
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalPromptHeader
import com.tien.tensor.ui.component.TerminalSectionLabel
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

private const val BAR_CELLS = 12

@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InsightsViewModel = viewModel(factory = AppModule.insightsViewModelFactory())
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val colors  = LauncherTheme.colors
    val context = LocalContext.current

    // Re-check usage access whenever the screen resumes (e.g. returning from system settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshUsage()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ACTIVITY_RECOGNITION is a runtime permission from API 29
    var hasStepsPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasStepsPermission = granted
        if (granted) viewModel.startStepTracking()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = TensorSpacing.screenH)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(TensorSpacing.md))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TerminalPromptHeader(path = "insights")
            TerminalButton(label = stringResource(R.string.common_back), onClick = onNavigateBack)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // ── Screen time ───────────────────────────────────────────────────────
        TerminalSectionLabel(label = stringResource(R.string.insights_screen_time))
        Spacer(Modifier.height(TensorSpacing.sm))

        if (!state.hasUsagePermission) {
            Text(
                text  = stringResource(R.string.insights_usage_rationale),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onBackground
            )
            Spacer(Modifier.height(TensorSpacing.xs))
            TerminalButton(
                label    = stringResource(R.string.insights_grant_usage),
                onClick  = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text  = stringResource(R.string.insights_total, formatDuration(state.totalScreenTimeMs)),
                style = MaterialTheme.typography.titleMedium,
                color = colors.primary
            )
            Spacer(Modifier.height(TensorSpacing.sm))
            if (state.usageStats.isEmpty()) {
                Text(text = stringResource(R.string.insights_no_usage), style = MaterialTheme.typography.bodySmall, color = colors.onSurface)
            } else {
                val maxTime = state.usageStats.first().totalTimeMs
                state.usageStats.forEach { stat ->
                    UsageRow(stat = stat, maxTimeMs = maxTime)
                    Spacer(Modifier.height(TensorSpacing.xs))
                }
            }
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // ── Steps ─────────────────────────────────────────────────────────────
        TerminalSectionLabel(label = stringResource(R.string.insights_steps))
        Spacer(Modifier.height(TensorSpacing.sm))

        when {
            !hasStepsPermission -> {
                Text(
                    text  = stringResource(R.string.insights_steps_rationale),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onBackground
                )
                Spacer(Modifier.height(TensorSpacing.xs))
                TerminalButton(
                    label    = stringResource(R.string.insights_grant_activity),
                    onClick  = { permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            !state.steps.sensorAvailable ->
                Text(text = stringResource(R.string.insights_no_sensor), style = MaterialTheme.typography.bodySmall, color = colors.onSurface)
            else -> {
                Text(text = "${state.steps.stepsToday}", style = MaterialTheme.typography.displayMedium, color = colors.primary)
                Text(text = stringResource(R.string.insights_steps_since_midnight), style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
            }
        }

        Spacer(Modifier.height(TensorSpacing.md))
    }
}

@Composable
private fun UsageRow(stat: AppUsageStat, maxTimeMs: Long) {
    val colors = LauncherTheme.colors
    val filled = if (maxTimeMs > 0) ((stat.totalTimeMs * BAR_CELLS) / maxTimeMs).toInt().coerceIn(1, BAR_CELLS) else 1
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "> ${stat.appName}", style = MaterialTheme.typography.bodyMedium, color = colors.onBackground)
            Text(text = formatDuration(stat.totalTimeMs), style = MaterialTheme.typography.labelSmall, color = colors.terminalPrompt)
        }
        Text(
            text  = "█".repeat(filled) + "·".repeat(BAR_CELLS - filled),
            style = MaterialTheme.typography.labelSmall,
            color = colors.primaryDim
        )
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = ms / 60_000
    return when {
        minutes < 1   -> "<1m"
        minutes < 60  -> "${minutes}m"
        else          -> "${minutes / 60}h ${minutes % 60}m"
    }
}
