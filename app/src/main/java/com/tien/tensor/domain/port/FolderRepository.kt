package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.AppFolder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getFolders(): Flow<List<AppFolder>>
    suspend fun createFolder(name: String): AppFolder
    suspend fun addApp(folderId: String, packageName: String): Boolean
    suspend fun removeApp(folderId: String, packageName: String)
    suspend fun deleteFolder(folderId: String)
}
