package com.tien.tensor.data.repository

import com.tien.tensor.data.source.FolderDataSource
import com.tien.tensor.domain.model.AppFolder
import com.tien.tensor.domain.port.FolderRepository
import kotlinx.coroutines.flow.Flow

class FolderRepositoryImpl(
    private val dataSource: FolderDataSource
) : FolderRepository {
    override fun getFolders(): Flow<List<AppFolder>>               = dataSource.folders
    override suspend fun createFolder(name: String): AppFolder     = dataSource.createFolder(name)
    override suspend fun addApp(folderId: String, packageName: String): Boolean = dataSource.addApp(folderId, packageName)
    override suspend fun removeApp(folderId: String, packageName: String)       = dataSource.removeApp(folderId, packageName)
    override suspend fun deleteFolder(folderId: String)                         = dataSource.deleteFolder(folderId)
}
