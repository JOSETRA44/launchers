package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.StepData
import com.tien.tensor.domain.port.StepCounterRepository
import kotlinx.coroutines.flow.Flow

class GetStepsUseCase(private val stepCounterRepository: StepCounterRepository) {
    operator fun invoke(): Flow<StepData> = stepCounterRepository.getSteps()
}
