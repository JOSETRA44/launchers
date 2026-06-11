package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.FolderRepository

class DeleteFolderUseCase(private val repository: FolderRepository) {
    suspend operator fun invoke(folderId: String) = repository.deleteFolder(folderId)
}
