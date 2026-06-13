package com.tien.tensor.domain.port

import kotlinx.coroutines.flow.Flow

/**
 * Stores the user's wallpaper sticker image. The image is referenced by an
 * opaque string: [setWallpaper] receives a platform Uri as text (the domain
 * never sees android.net.Uri) and [observeWallpaper] emits the absolute path
 * of the imported copy, or null when no wallpaper is set.
 */
interface WallpaperRepository {
    fun observeWallpaper(): Flow<String?>
    suspend fun setWallpaper(uri: String)
    suspend fun clearWallpaper()
}
