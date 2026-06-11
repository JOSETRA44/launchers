package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.port.ArsenalRegistry

class GetArsenalModulesUseCase(private val registry: ArsenalRegistry) {
    operator fun invoke(): List<ModuleMeta> = registry.all().map { it.meta }
}
