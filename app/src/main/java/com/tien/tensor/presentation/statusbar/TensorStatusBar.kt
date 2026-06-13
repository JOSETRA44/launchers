package com.tien.tensor.presentation.statusbar

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tien.tensor.domain.model.BarSize
import com.tien.tensor.domain.model.NetworkType
import com.tien.tensor.ui.component.BatteryGlyph
import com.tien.tensor.ui.component.SignalBars
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

private const val LOW_BATTERY = 15

/**
 * The launcher's own status bar — replaces the hidden system bar.
 * Left: clock + power-save tag. Right: live network + battery indicators.
 *
 * Tapping the network zone while on generic CELL requests the optional
 * READ_PHONE_STATE permission so the exact generation (5G/4G/...) can show.
 */
@Composable
fun TensorStatusBar(
    viewModel: StatusBarViewModel,
    barSize: BarSize = BarSize.NORMAL,
    showDate: Boolean = false,
    opaque: Boolean = true,
) {
    val state  by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LauncherTheme.colors
    val status = state.status

    val scale = when (barSize) {
        BarSize.COMPACT -> 0.85f
        BarSize.NORMAL  -> 1f
        BarSize.LARGE   -> 1.3f
    }
    val barPadding = when (barSize) {
        BarSize.COMPACT -> 3.dp
        BarSize.NORMAL  -> 6.dp
        BarSize.LARGE   -> 10.dp
    }
    val timeStyle = when (barSize) {
        BarSize.COMPACT -> MaterialTheme.typography.labelSmall
        BarSize.NORMAL  -> MaterialTheme.typography.labelMedium
        BarSize.LARGE   -> MaterialTheme.typography.labelLarge
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.restartStatusCollection()
    }

    val bgModifier = if (opaque) Modifier.fillMaxWidth().background(colors.background) else Modifier.fillMaxWidth()
    Column(modifier = bgModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TensorSpacing.screenH, vertical = barPadding),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Clock + optional date + power save ───────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = state.time, style = timeStyle, color = colors.primary)
                    if (showDate) {
                        Text(text = state.date, style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
                    }
                }
                if (status.isPowerSave) {
                    Spacer(Modifier.width(TensorSpacing.sm))
                    Text(text = "ECO", style = MaterialTheme.typography.labelSmall, color = colors.cursor)
                }
            }

            // ── Network + battery ─────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TensorSpacing.sm)
            ) {
                val canUpgrade  = status.networkType == NetworkType.CELLULAR
                val interaction = remember { MutableInteractionSource() }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs),
                    modifier = Modifier.clickable(
                        interactionSource = interaction,
                        indication        = null,
                        enabled           = canUpgrade,
                        onClick           = { permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE) }
                    )
                ) {
                    if (status.networkType != NetworkType.NONE) {
                        SignalBars(level = status.signalLevel, color = colors.primary, dimColor = colors.primaryDim, scale = scale)
                    }
                    Text(
                        text  = status.networkType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (status.networkType == NetworkType.NONE) colors.onSurface else colors.terminalPrompt
                    )
                }

                val batteryColor = when {
                    status.isCharging                     -> colors.primary
                    status.batteryPercent <= LOW_BATTERY  -> colors.error
                    else                                  -> colors.terminalPrompt
                }
                BatteryGlyph(percent = status.batteryPercent, charging = status.isCharging, color = batteryColor, scale = scale)
                Text(
                    text  = "${status.batteryPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = batteryColor
                )
            }
        }
        // Hairline separating the bar from the content below
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}
