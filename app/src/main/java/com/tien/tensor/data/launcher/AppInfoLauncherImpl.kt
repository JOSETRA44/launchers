package com.tien.tensor.data.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.tien.tensor.domain.port.AppInfoLauncher

class AppInfoLauncherImpl(private val context: Context) : AppInfoLauncher {
    override fun open(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
