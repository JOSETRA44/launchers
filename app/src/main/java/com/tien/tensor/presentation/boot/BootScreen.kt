package com.tien.tensor.presentation.boot

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.tien.tensor.ui.component.BlinkingCursor
import com.tien.tensor.ui.component.TypewriterText
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing
import kotlinx.coroutines.delay

private val BOOT_LINES = listOf(
    "TENSOR KERNEL [INIT]",
    "> Loading subsystems... [OK]",
    "> Mounting partitions... [OK]",
    "> Security handshake... [OK]",
    "> Indexing applications... [OK]",
    "TENSOR OS [v1.0.0] — READY"
)
private const val CHAR_DELAY_MS = 22L
private const val LINE_GAP_MS   = 180L
private const val FINAL_PAUSE   = 600L
private const val FADE_DURATION = 500

@Composable
fun BootScreen(onBootComplete: () -> Unit) {
    val colors = LauncherTheme.colors

    // currentLine = index of the line currently being typed (0-based)
    // when == BOOT_LINES.size, all lines are done
    var currentLine by remember { mutableIntStateOf(0) }
    var targetAlpha  by remember { mutableFloatStateOf(1f) }
    val alpha by animateFloatAsState(
        targetValue   = targetAlpha,
        animationSpec = tween(durationMillis = FADE_DURATION),
        label         = "boot_fade"
    )

    LaunchedEffect(Unit) {
        BOOT_LINES.forEach { line ->
            delay(line.length * CHAR_DELAY_MS + LINE_GAP_MS)
            currentLine++
        }
        delay(FINAL_PAUSE)
        targetAlpha = 0f
        delay((FADE_DURATION + 100).toLong())
        onBootComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .graphicsLayer { this.alpha = alpha }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(TensorSpacing.screenH)
        ) {
            Spacer(Modifier.height(TensorSpacing.xl))

            // Static completed lines
            for (i in 0 until minOf(currentLine, BOOT_LINES.size)) {
                val isLast = i == BOOT_LINES.lastIndex
                Text(
                    text  = BOOT_LINES[i],
                    style = if (isLast) MaterialTheme.typography.titleMedium
                            else       MaterialTheme.typography.bodySmall,
                    color = if (isLast) colors.primary else colors.onBackground
                )
                Spacer(Modifier.height(TensorSpacing.xs))
            }

            // Currently typing line
            if (currentLine < BOOT_LINES.size) {
                val isLast = currentLine == BOOT_LINES.lastIndex
                TypewriterText(
                    fullText     = BOOT_LINES[currentLine],
                    style        = if (isLast) MaterialTheme.typography.titleMedium
                                   else        MaterialTheme.typography.bodySmall,
                    color        = if (isLast) colors.primary else colors.onBackground,
                    charDelayMs  = CHAR_DELAY_MS
                )
                Spacer(Modifier.height(TensorSpacing.xs))
            }

            // Blinking cursor shown while boot is in progress
            if (currentLine < BOOT_LINES.size) {
                BlinkingCursor(style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
