package com.tien.tensor.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import com.tien.tensor.ui.theme.LauncherTheme

@Composable
fun AppIcon(
    packageName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LauncherTheme.colors

    val painter = remember(packageName) {
        runCatching {
            val bitmap = context.packageManager
                .getApplicationIcon(packageName)
                .toBitmap()
                .asImageBitmap()
            BitmapPainter(bitmap)
        }.getOrNull()
    }

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = packageName.firstOrNull()?.uppercase() ?: "?",
                color = colors.primary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
