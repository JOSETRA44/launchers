package com.tien.tensor.data.repository

import com.tien.tensor.data.source.WallpaperDataSource
import com.tien.tensor.domain.port.WallpaperRepository
import kotlinx.coroutines.flow.Flow

class WallpaperRepositoryImpl(
    private val dataSource: WallpaperDataSource
) : WallpaperRepository {
    override fun observeWallpaper(): Flow<String?> = dataSource.path
    override suspend fun setWallpaper(uri: String) = dataSource.import(uri)
    override suspend fun clearWallpaper() = dataSource.clear()
}
