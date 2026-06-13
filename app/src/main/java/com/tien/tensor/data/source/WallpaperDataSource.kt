package com.tien.tensor.data.source

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Owns the wallpaper image file. The picked content-Uri is copied into
 * internal storage (`filesDir/wallpaper/wp_<timestamp>`) so the launcher never
 * depends on a revocable Uri permission. Each import uses a fresh filename and
 * deletes the previous file — a path change is what drives recomposition of
 * the wallpaper layer, so no extra "version" bookkeeping is needed.
 */
class WallpaperDataSource(private val context: Context) {

    private val dir = File(context.filesDir, "wallpaper")

    private val _path = MutableStateFlow(currentFile()?.absolutePath)
    val path: StateFlow<String?> = _path.asStateFlow()

    /** Copies the image behind [uriString] into internal storage. */
    suspend fun import(uriString: String) = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        val target = File(dir.apply { mkdirs() }, "wp_${System.currentTimeMillis()}")
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext
        }.onFailure {
            target.delete()
            return@withContext
        }
        deleteAllExcept(target)
        _path.value = target.absolutePath
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        deleteAllExcept(null)
        _path.value = null
    }

    private fun currentFile(): File? =
        dir.listFiles()?.filter { it.isFile }?.maxByOrNull { it.name }

    private fun deleteAllExcept(keep: File?) {
        dir.listFiles()?.forEach { if (it != keep) it.delete() }
    }
}
