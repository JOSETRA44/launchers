package com.tien.tensor.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Minimal geometric hardware indicators for the dynamic status bar.
 * Pure Canvas — no vector icon resources, fully theme-driven.
 */

private const val BAR_COUNT = 4

/** Four ascending signal bars; bars at index < level render in [color], the rest in [dimColor]. */
@Composable
fun SignalBars(
    level: Int,
    color: Color,
    dimColor: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Canvas(modifier = modifier.size(width = 16.dp * scale, height = 11.dp * scale)) {
        val gap      = 2.dp.toPx()
        val barW     = (size.width - gap * (BAR_COUNT - 1)) / BAR_COUNT
        val minH     = size.height * 0.3f
        val stepH    = (size.height - minH) / (BAR_COUNT - 1)
        for (i in 0 until BAR_COUNT) {
            val h = minH + stepH * i
            drawRect(
                color   = if (i < level) color else dimColor,
                topLeft = Offset(i * (barW + gap), size.height - h),
                size    = Size(barW, h)
            )
        }
    }
}

/** Battery cell outline with proportional fill, terminal nub and a bolt when charging. */
@Composable
fun BatteryGlyph(
    percent: Int,
    charging: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Canvas(modifier = modifier.size(width = 26.dp * scale, height = 11.dp * scale)) {
        val stroke  = 1.dp.toPx()
        val nubW    = 2.dp.toPx()
        val boltW   = if (charging) 7.dp.toPx() else 0f
        val bodyW   = size.width - nubW - boltW
        val bodyH   = size.height
        val corner  = CornerRadius(2.dp.toPx())

        // Bolt to the left of the cell (charging only)
        if (charging) {
            val bw = boltW - 2.dp.toPx()
            val bolt = Path().apply {
                moveTo(bw * 0.62f, 0f)
                lineTo(bw * 0.10f, bodyH * 0.60f)
                lineTo(bw * 0.46f, bodyH * 0.60f)
                lineTo(bw * 0.38f, bodyH)
                lineTo(bw * 0.90f, bodyH * 0.40f)
                lineTo(bw * 0.54f, bodyH * 0.40f)
                close()
            }
            drawPath(bolt, color)
        }

        // Cell body
        drawRoundRect(
            color        = color,
            topLeft      = Offset(boltW, 0f),
            size         = Size(bodyW, bodyH),
            cornerRadius = corner,
            style        = Stroke(width = stroke)
        )
        // Terminal nub
        drawRect(
            color   = color,
            topLeft = Offset(boltW + bodyW, bodyH * 0.3f),
            size    = Size(nubW, bodyH * 0.4f)
        )
        // Proportional fill
        val inset = stroke * 2
        val fillW = (bodyW - inset * 2) * (percent.coerceIn(0, 100) / 100f)
        if (fillW > 0f) {
            drawRect(
                color   = color,
                topLeft = Offset(boltW + inset, inset),
                size    = Size(fillW, bodyH - inset * 2)
            )
        }
    }
}
