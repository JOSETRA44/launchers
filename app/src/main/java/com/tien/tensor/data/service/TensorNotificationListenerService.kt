package com.tien.tensor.data.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton holding the live notification counts exposed to the rest of the app.
 * Using an object here is intentional: NotificationListenerService is a system singleton,
 * and this is the standard in-process bridge pattern to share its state.
 */
object NotificationStore {
    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val counts: StateFlow<Map<String, Int>> = _counts.asStateFlow()

    internal fun update(map: Map<String, Int>) { _counts.value = map }
    internal fun clear()                        { _counts.value = emptyMap() }
}

class TensorNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected()                            = refresh()
    override fun onListenerDisconnected()                         = NotificationStore.clear()
    override fun onNotificationPosted(sbn: StatusBarNotification?) = refresh()
    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    private fun refresh() {
        val active = try { activeNotifications ?: return } catch (_: Exception) { return }
        NotificationStore.update(
            active
                .filter { !it.isOngoing }
                .groupBy { it.packageName }
                .mapValues { it.value.size }
        )
    }
}
