package com.tien.tensor.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.tien.tensor.domain.model.WallpaperAnchor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders the user's wallpaper sticker behind the launcher content.
 *
 * The file is decoded off the main thread once per [path] (each import writes
 * a fresh filename, so a new image is always a new key — no manual cache
 * invalidation). Transparent PNGs render as floating stickers over the themed
 * background; [WallpaperAnchor.FILL] switches to a classic full-bleed
 * wallpaper (crop-to-fill, [sizePct] ignored).
 */
@Composable
fun WallpaperLayer(
    path: String,
    alpha: Float,
    sizePct: Int,
    anchor: WallpaperAnchor,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, path) {
        value = withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
        }
    }
    val image = bitmap ?: return

    Box(modifier = modifier.fillMaxSize()) {
        if (anchor == WallpaperAnchor.FILL) {
            Image(
                bitmap             = image,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                alpha              = alpha,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            Image(
                bitmap             = image,
                contentDescription = null,
                contentScale       = ContentScale.Fit,
                alpha              = alpha,
                modifier           = Modifier
                    .fillMaxWidth(sizePct / 100f)
                    .align(anchor.toAlignment())
            )
        }
    }
}

private fun WallpaperAnchor.toAlignment(): Alignment = when (this) {
    WallpaperAnchor.TOP_LEFT     -> Alignment.TopStart
    WallpaperAnchor.TOP_RIGHT    -> Alignment.TopEnd
    WallpaperAnchor.CENTER       -> Alignment.Center
    WallpaperAnchor.BOTTOM_LEFT  -> Alignment.BottomStart
    WallpaperAnchor.BOTTOM_RIGHT -> Alignment.BottomEnd
    WallpaperAnchor.FILL         -> Alignment.Center
}
