package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.AppFolder
import com.tien.tensor.domain.port.FolderRepository
import kotlinx.coroutines.flow.Flow

class GetFoldersUseCase(private val repository: FolderRepository) {
    operator fun invoke(): Flow<List<AppFolder>> = repository.getFolders()
}
