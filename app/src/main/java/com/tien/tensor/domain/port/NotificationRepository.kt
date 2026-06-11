package com.tien.tensor.domain.port

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationCounts(): Flow<Map<String, Int>>
}
