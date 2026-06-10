package com.tien.tensor.data.repository

import com.tien.tensor.data.source.PinnedAppsDataSource
import com.tien.tensor.domain.model.PinnedApp
import com.tien.tensor.domain.port.PinnedAppsRepository
import kotlinx.coroutines.flow.Flow

class PinnedAppsRepositoryImpl(
    private val dataSource: PinnedAppsDataSource
) : PinnedAppsRepository {
    override fun getPinnedApps(): Flow<List<PinnedApp>> = dataSource.pinnedApps
    override suspend fun pinApp(packageName: String, appName: String): Boolean = dataSource.pinApp(packageName, appName)
    override suspend fun unpinApp(packageName: String) = dataSource.unpinApp(packageName)
    override suspend fun reorder(packageNames: List<String>) = dataSource.reorder(packageNames)
}
