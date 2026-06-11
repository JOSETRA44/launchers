package com.tien.tensor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.tien.tensor.domain.model.ThemeId

@Composable
fun TensorTheme(
    themeId: ThemeId = ThemeId.HACKER_DARK,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val colors     = themeColors(themeId)
    val typography = remember(fontScale) { terminalTypography(fontScale) }

    val materialColorScheme = darkColorScheme(
        primary            = colors.primary,
        onPrimary          = colors.background,
        primaryContainer   = colors.primaryDim,
        secondary          = colors.onSurface,
        onSecondary        = colors.background,
        background         = colors.background,
        onBackground       = colors.onBackground,
        surface            = colors.surface,
        onSurface          = colors.onSurface,
        surfaceVariant     = colors.surfaceVariant,
        onSurfaceVariant   = colors.onBackground,
        error              = colors.error,
        onError            = colors.background,
        outline            = colors.border
    )

    CompositionLocalProvider(LocalLauncherColors provides colors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography  = typography,
            content     = content
        )
    }
}
