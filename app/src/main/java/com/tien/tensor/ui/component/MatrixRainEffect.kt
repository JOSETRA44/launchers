package com.tien.tensor.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

private const val TRAIL_LENGTH   = 14   // glyphs per falling column
private const val GLYPH_CYCLE_MS = 90L  // how often a glyph mutates

/**
 * Matrix-style digital rain. Fully deterministic from frame time — column
 * speed, phase and glyph choice derive from hashes, so no per-frame state
 * is mutated and no allocations happen inside the draw loop.
 */
@Composable
fun MatrixRainEffect(
    modifier: Modifier = Modifier,
    color: Color,
    maxAlpha: Float = 0.45f,
    charset: String = "01<>/\\|#$%&*+-=?"
) {
    val textMeasurer = rememberTextMeasurer()
    val glyphStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp) }
    val glyphs = remember(charset) {
        charset.map { textMeasurer.measure(AnnotatedString(it.toString()), glyphStyle) }
    }

    var timeMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) withFrameMillis { timeMs = it - start }
    }

    Canvas(modifier = modifier) {
        val cellW = glyphs.first().size.width.toFloat() + 6f
        val cellH = glyphs.first().size.height.toFloat()
        val columns = (size.width / cellW).toInt().coerceAtLeast(1)
        val trailPx = TRAIL_LENGTH * cellH
        val travel  = size.height + trailPx

        for (col in 0 until columns) {
            val speed  = 0.06f + hash01(col) * 0.18f            // px per ms
            val phase  = hash01(col + 7919) * travel
            val headY  = (timeMs * speed + phase) % travel - trailPx
            val x      = col * cellW

            for (k in 0 until TRAIL_LENGTH) {
                val y = headY + (TRAIL_LENGTH - 1 - k) * cellH
                if (y < -cellH || y > size.height) continue
                val row   = ((y + travel) / cellH).toInt()
                val tick  = (timeMs / GLYPH_CYCLE_MS).toInt()
                val glyph = glyphs[pseudoIndex(col, row, tick, glyphs.size)]
                // Head glyph is brightest; the trail fades out behind it
                val fade  = if (k == TRAIL_LENGTH - 1) 1f else (k.toFloat() / TRAIL_LENGTH) * 0.7f
                drawText(
                    textLayoutResult = glyph,
                    color            = color,
                    topLeft          = Offset(x, y),
                    alpha            = (maxAlpha * fade).coerceIn(0f, 1f)
                )
            }
        }
    }
}

/** Deterministic [0,1) hash so columns keep stable speed/phase across frames. */
private fun hash01(seed: Int): Float {
    var x = seed * 374761393 + 668265263
    x = (x xor (x shr 13)) * 1274126177
    return ((x xor (x shr 16)) and 0x7FFFFFFF) / Int.MAX_VALUE.toFloat()
}

private fun pseudoIndex(col: Int, row: Int, tick: Int, size: Int): Int {
    val h = (col * 31 + row * 17 + tick * 13) and 0x7FFFFFFF
    return h % size
}
