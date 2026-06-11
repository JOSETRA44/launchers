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

private val amberTerm = LauncherColors(
    background    = Color(0xFF0A0700),
    surface       = Color(0xFF140E02),
    surfaceVariant= Color(0xFF1E1604),
    primary       = Color(0xFFFFB000),
    primaryDim    = Color(0xFF4D3500),
    onBackground  = Color(0xFFC9A86A),
    onSurface     = Color(0xFF8A7448),
    cursor        = Color(0xFFFFC933),
    border        = Color(0xFF3A2C08),
    error         = Color(0xFFFF4444),
    terminalPrompt= Color(0xFFE09E00)
)

private val redAlert = LauncherColors(
    background    = Color(0xFF0A0000),
    surface       = Color(0xFF140404),
    surfaceVariant= Color(0xFF1E0808),
    primary       = Color(0xFFFF2B2B),
    primaryDim    = Color(0xFF4D0D0D),
    onBackground  = Color(0xFFC98A8A),
    onSurface     = Color(0xFF8A5555),
    cursor        = Color(0xFFFF5555),
    border        = Color(0xFF3A0E0E),
    error         = Color(0xFFFFB300),
    terminalPrompt= Color(0xFFCC2222)
)

private val arcticIce = LauncherColors(
    background    = Color(0xFF06080C),
    surface       = Color(0xFF0B1018),
    surfaceVariant= Color(0xFF101826),
    primary       = Color(0xFF9EC9FF),
    primaryDim    = Color(0xFF1E3A5C),
    onBackground  = Color(0xFFA8B8CC),
    onSurface     = Color(0xFF5C7088),
    cursor        = Color(0xFFD6E9FF),
    border        = Color(0xFF16263A),
    error         = Color(0xFFFF4D6A),
    terminalPrompt= Color(0xFF7FB3E8)
)

fun themeColors(id: ThemeId): LauncherColors = when (id) {
    ThemeId.HACKER_DARK  -> hackerDark
    ThemeId.HACKER_CYAN  -> hackerCyan
    ThemeId.MATRIX_GREEN -> matrixGreen
    ThemeId.AMBER_TERM   -> amberTerm
    ThemeId.RED_ALERT    -> redAlert
    ThemeId.ARCTIC_ICE   -> arcticIce
}

val LocalLauncherColors = staticCompositionLocalOf { hackerDark }

object LauncherTheme {
    val colors: LauncherColors
        @Composable get() = LocalLauncherColors.current
}
