package com.tien.tensor.ui.component

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

/** Composition-local typing delay in ms-per-character. Defaults to 55 (normal feel). */
val LocalTypingSpeed = compositionLocalOf { 55 }

@Composable
fun TypewriterText(
    fullText: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    charDelayMs: Long = LocalTypingSpeed.current.toLong(),
    onComplete: (() -> Unit)? = null
) {
    var visibleCount by remember(fullText) { mutableIntStateOf(0) }

    LaunchedEffect(fullText, charDelayMs) {
        visibleCount = 0
        if (charDelayMs <= 0L) {
            visibleCount = fullText.length
            onComplete?.invoke()
            return@LaunchedEffect
        }
        fullText.indices.forEach { index ->
            delay(charDelayMs)
            visibleCount = index + 1
        }
        onComplete?.invoke()
    }

    Text(
        text = fullText.take(visibleCount),
        modifier = modifier,
        style = style,
        color = color
    )
}
