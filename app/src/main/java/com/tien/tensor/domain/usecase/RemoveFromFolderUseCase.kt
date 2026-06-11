package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.FolderRepository

class RemoveFromFolderUseCase(private val repository: FolderRepository) {
    suspend operator fun invoke(folderId: String, packageName: String) =
        repository.removeApp(folderId, packageName)
}
