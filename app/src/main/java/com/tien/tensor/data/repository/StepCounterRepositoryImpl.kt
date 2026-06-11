package com.tien.tensor.data.repository

import com.tien.tensor.data.source.StepCounterDataSource
import com.tien.tensor.domain.model.StepData
import com.tien.tensor.domain.port.StepCounterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class StepCounterRepositoryImpl(
    private val stepCounterDataSource: StepCounterDataSource
) : StepCounterRepository {

    override fun getSteps(): Flow<StepData> =
        stepCounterDataSource.getSteps().flowOn(Dispatchers.IO)
}
