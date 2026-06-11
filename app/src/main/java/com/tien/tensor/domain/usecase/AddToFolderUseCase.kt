package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.FolderRepository

class AddToFolderUseCase(private val repository: FolderRepository) {
    suspend operator fun invoke(folderId: String, packageName: String): Boolean =
        repository.addApp(folderId, packageName)
}
