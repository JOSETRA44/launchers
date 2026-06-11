package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.StepData
import kotlinx.coroutines.flow.Flow

interface StepCounterRepository {
    fun getSteps(): Flow<StepData>
}
