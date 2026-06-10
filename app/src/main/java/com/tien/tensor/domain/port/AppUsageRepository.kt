package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.AppUsageStats
import kotlinx.coroutines.flow.Flow

interface AppUsageRepository {
    /** Emits the full aggregated stats list, updating whenever history changes. */
    fun getUsageStats(): Flow<List<AppUsageStats>>
    suspend fun trackLaunch(packageName: String)
    suspend fun clearAllHistory()
}
