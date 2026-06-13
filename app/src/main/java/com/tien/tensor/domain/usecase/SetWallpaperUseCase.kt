package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.WallpaperRepository

/** Imports the image behind [uri] (a platform Uri as text) as the wallpaper. */
class SetWallpaperUseCase(private val repository: WallpaperRepository) {
    suspend operator fun invoke(uri: String) = repository.setWallpaper(uri)
}
