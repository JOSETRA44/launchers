package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.port.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationCountsUseCase(private val repository: NotificationRepository) {
    operator fun invoke(): Flow<Map<String, Int>> = repository.getNotificationCounts()
}
