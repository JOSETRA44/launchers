package com.tien.tensor.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.tien.tensor.domain.model.ThemeId

data class LauncherColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryDim: Color,
    val onBackground: Color,
    val onSurface: Color,
    val cursor: Color,
    val border: Color,
    val error: Color,
    val terminalPrompt: Color
)

private val hackerDark = LauncherColors(
    background    = Color(0xFF090909),
    surface       = Color(0xFF111111),
    surfaceVariant= Color(0xFF1A1A1A),
    primary       = Color(0xFF00FF41),
    primaryDim    = Color(0xFF004D18),
    onBackground  = Color(0xFFB0B0B0),
    onSurface     = Color(0xFF787878),
    cursor        = Color(0xFF39FF14),
    border        = Color(0xFF1E3B1E),
    error         = Color(0xFFFF3333),
    terminalPrompt= Color(0xFF00CC35)
)

private val hackerCyan = LauncherColors(
    background    = Color(0xFF050F10),
    surface       = Color(0xFF0B1A1C),
    surfaceVariant= Color(0xFF0F2224),
    primary       = Color(0xFF00E5FF),
    primaryDim    = Color(0xFF004D55),
    onBackground  = Color(0xFF90C0C5),
    onSurface     = Color(0xFF558888),
    cursor        = Color(0xFF00FFFF),
    border        = Color(0xFF0D3035),
    error         = Color(0xFFFF4444),
    terminalPrompt= Color(0xFF00BBCC)
)

private val matrixGreen = LauncherColors(
    background    = Color(0xFF000000),
    surface       = Color(0xFF050805),
    surfaceVariant= Color(0xFF0A100A),
    primary       = Color(0xFF20C20E),
    primaryDim    = Color(0xFF0A4005),
    onBackground  = Color(0xFF00B800),
    onSurface     = Color(0xFF007500),
    cursor        = Color(0xFF7FFF00),
    border        = Color(0xFF0A1A0A),
    error         = Color(0xFFFF0000),
    terminalPrompt= Color(0xFF17A10C)
)

fun themeColors(id: ThemeId): LauncherColors = when (id) {
    ThemeId.HACKER_DARK  -> hackerDark
    ThemeId.HACKER_CYAN  -> hackerCyan
    ThemeId.MATRIX_GREEN -> matrixGreen
}

val LocalLauncherColors = staticCompositionLocalOf { hackerDark }

object LauncherTheme {
    val colors: LauncherColors
        @Composable get() = LocalLauncherColors.current
}
