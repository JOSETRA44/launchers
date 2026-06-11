package com.tien.tensor.presentation.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tien.tensor.R
import com.tien.tensor.ui.component.MatrixRainEffect
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

/**
 * Full-screen "digital rain" shown automatically while the device charges.
 * Tap anywhere to dismiss; it also disappears when the charger is unplugged.
 */
@Composable
fun ChargingOverlay(batteryPercent: Int, onDismiss: () -> Unit) {
    val colors = LauncherTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onDismiss)
    ) {
        MatrixRainEffect(modifier = Modifier.fillMaxSize(), color = colors.primary, maxAlpha = 0.5f)

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text  = stringResource(R.string.charging_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.terminalPrompt
            )
            Spacer(Modifier.height(TensorSpacing.sm))
            Text(
                text  = "$batteryPercent%",
                style = MaterialTheme.typography.displayLarge,
                color = colors.primary
            )
            Spacer(Modifier.height(TensorSpacing.sm))
            Text(
                text  = batteryBar(batteryPercent),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary
            )
            Spacer(Modifier.height(TensorSpacing.xl))
            Text(
                text  = stringResource(R.string.charging_tap_dismiss),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface
            )
        }
    }
}

private fun batteryBar(percent: Int, cells: Int = 20): String {
    val filled = (percent.coerceIn(0, 100) * cells) / 100
    return "[" + "█".repeat(filled) + "·".repeat(cells - filled) + "]"
}
