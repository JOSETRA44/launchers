package com.tien.tensor.data.launcher

import android.content.Context
import android.content.Intent
import com.tien.tensor.domain.port.AppLauncher

class AndroidAppLauncher(private val context: Context) : AppLauncher {

    override fun launch(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        context.startActivity(intent)
    }
}
