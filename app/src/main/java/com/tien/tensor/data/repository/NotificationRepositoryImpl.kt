package com.tien.tensor.data.repository

import com.tien.tensor.data.service.NotificationStore
import com.tien.tensor.domain.port.NotificationRepository
import kotlinx.coroutines.flow.Flow

class NotificationRepositoryImpl : NotificationRepository {
    override fun getNotificationCounts(): Flow<Map<String, Int>> = NotificationStore.counts
}
