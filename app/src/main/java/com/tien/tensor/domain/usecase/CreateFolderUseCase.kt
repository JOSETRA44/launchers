package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.AppFolder
import com.tien.tensor.domain.port.FolderRepository

class CreateFolderUseCase(private val repository: FolderRepository) {
    suspend operator fun invoke(name: String): AppFolder = repository.createFolder(name)
}
