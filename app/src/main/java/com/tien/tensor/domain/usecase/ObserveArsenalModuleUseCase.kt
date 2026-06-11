package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.port.ArsenalRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class ObserveArsenalModuleUseCase(private val registry: ArsenalRegistry) {
    operator fun invoke(moduleId: String): Flow<ModuleReport> =
        registry.byId(moduleId)?.observe() ?: emptyFlow()
}
