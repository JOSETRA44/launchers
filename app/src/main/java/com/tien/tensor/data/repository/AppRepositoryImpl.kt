package com.tien.tensor.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.tien.tensor.data.source.AppDataSource
import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.domain.port.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class AppRepositoryImpl(
    private val appDataSource: AppDataSource,
    private val context: Context
) : AppRepository {

    override fun getInstalledApps(): Flow<List<AppInfo>> = callbackFlow {
        trySend(appDataSource.queryInstalledApps())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                trySend(appDataSource.queryInstalledApps())
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter)

        awaitClose { context.unregisterReceiver(receiver) }
    }.flowOn(Dispatchers.IO)
}
