package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getInstalledApps(): Flow<List<AppInfo>>
}
