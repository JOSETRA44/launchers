package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.PinnedApp
import kotlinx.coroutines.flow.Flow

interface PinnedAppsRepository {
    fun getPinnedApps(): Flow<List<PinnedApp>>
    suspend fun pinApp(packageName: String, appName: String): Boolean
    suspend fun unpinApp(packageName: String)
    suspend fun reorder(packageNames: List<String>)
}
