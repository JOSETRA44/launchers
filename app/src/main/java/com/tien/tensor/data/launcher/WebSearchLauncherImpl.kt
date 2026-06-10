package com.tien.tensor.data.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tien.tensor.domain.port.WebSearchLauncher

class WebSearchLauncherImpl(private val context: Context) : WebSearchLauncher {
    override fun search(query: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
