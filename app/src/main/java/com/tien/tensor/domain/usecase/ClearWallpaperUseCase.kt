package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.WallpaperRepository

/** Removes the wallpaper image and frees its stored copy. */
class ClearWallpaperUseCase(private val repository: WallpaperRepository) {
    suspend operator fun invoke() = repository.clearWallpaper()
}
