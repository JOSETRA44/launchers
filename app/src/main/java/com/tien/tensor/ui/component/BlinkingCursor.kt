package com.tien.tensor.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.tien.tensor.ui.theme.LauncherTheme

/** Composition-local that drives cursor animation across the tree. Defaults to true (blinking). */
val LocalCursorBlink = compositionLocalOf { true }

@Composable
fun BlinkingCursor(
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    val colors = LauncherTheme.colors
    val blink  = LocalCursorBlink.current

    if (!blink) {
        Text(text = "_", color = colors.cursor, modifier = modifier, style = style)
        return
    }

    val transition = rememberInfiniteTransition(label = "cursor_blink")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )
    Text(
        text = "█",
        color = colors.cursor.copy(alpha = alpha),
        modifier = modifier,
        style = style
    )
}
