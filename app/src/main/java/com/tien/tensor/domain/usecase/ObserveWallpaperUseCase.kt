package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.WallpaperRepository
import kotlinx.coroutines.flow.Flow

/** Emits the absolute path of the current wallpaper image, or null when unset. */
class ObserveWallpaperUseCase(private val repository: WallpaperRepository) {
    operator fun invoke(): Flow<String?> = repository.observeWallpaper()
}
