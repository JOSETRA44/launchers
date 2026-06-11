package com.tien.tensor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// System monospace (Roboto Mono / Droid Sans Mono).
// To use JetBrains Mono: add jetbrains_mono.ttf to res/font/ and reference it here.
val TerminalFontFamily = FontFamily.Monospace

/** Builds the terminal type ramp at a user-chosen scale (see `UiPrefs.fontScale`). */
fun terminalTypography(scale: Float = 1f) = Typography(
    displayLarge   = TextStyle(fontFamily = TerminalFontFamily, fontSize = (48 * scale).sp, fontWeight = FontWeight.Light),
    displayMedium  = TextStyle(fontFamily = TerminalFontFamily, fontSize = (36 * scale).sp, fontWeight = FontWeight.Light),
    headlineLarge  = TextStyle(fontFamily = TerminalFontFamily, fontSize = (28 * scale).sp, fontWeight = FontWeight.Normal),
    headlineMedium = TextStyle(fontFamily = TerminalFontFamily, fontSize = (22 * scale).sp, fontWeight = FontWeight.Normal),
    titleLarge     = TextStyle(fontFamily = TerminalFontFamily, fontSize = (18 * scale).sp, fontWeight = FontWeight.Medium),
    titleMedium    = TextStyle(fontFamily = TerminalFontFamily, fontSize = (16 * scale).sp, fontWeight = FontWeight.Medium, letterSpacing = 0.05.em),
    bodyLarge      = TextStyle(fontFamily = TerminalFontFamily, fontSize = (16 * scale).sp, fontWeight = FontWeight.Normal),
    bodyMedium     = TextStyle(fontFamily = TerminalFontFamily, fontSize = (14 * scale).sp, fontWeight = FontWeight.Normal),
    bodySmall      = TextStyle(fontFamily = TerminalFontFamily, fontSize = (12 * scale).sp, fontWeight = FontWeight.Normal),
    labelLarge     = TextStyle(fontFamily = TerminalFontFamily, fontSize = (14 * scale).sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.em),
    labelMedium    = TextStyle(fontFamily = TerminalFontFamily, fontSize = (12 * scale).sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.em),
    labelSmall     = TextStyle(fontFamily = TerminalFontFamily, fontSize = (10 * scale).sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.em)
)

val TerminalTypography = terminalTypography(1f)
