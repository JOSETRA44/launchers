package com.tien.tensor.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tien.tensor.domain.model.AppFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class FolderDataSource(private val dataStore: DataStore<Preferences>) {

    private val foldersKey = stringPreferencesKey("folders_v1")

    val folders: Flow<List<AppFolder>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> parse(prefs[foldersKey] ?: "[]") }

    suspend fun createFolder(name: String): AppFolder {
        val folder = AppFolder(UUID.randomUUID().toString(), name)
        dataStore.edit { prefs ->
            val current = parse(prefs[foldersKey] ?: "[]").toMutableList()
            current += folder
            prefs[foldersKey] = serialize(current)
        }
        return folder
    }

    suspend fun addApp(folderId: String, packageName: String): Boolean {
        val current = parse(dataStore.data.first()[foldersKey] ?: "[]")
        val folder = current.firstOrNull { it.id == folderId } ?: return false
        if (packageName in folder.packageNames) return false
        dataStore.edit { prefs ->
            val updated = parse(prefs[foldersKey] ?: "[]").map {
                if (it.id == folderId) it.copy(packageNames = it.packageNames + packageName) else it
            }
            prefs[foldersKey] = serialize(updated)
        }
        return true
    }

    suspend fun removeApp(folderId: String, packageName: String) {
        dataStore.edit { prefs ->
            val updated = parse(prefs[foldersKey] ?: "[]").map {
                if (it.id == folderId) it.copy(packageNames = it.packageNames - packageName) else it
            }
            prefs[foldersKey] = serialize(updated)
        }
    }

    suspend fun deleteFolder(folderId: String) {
        dataStore.edit { prefs ->
            prefs[foldersKey] = serialize(parse(prefs[foldersKey] ?: "[]").filter { it.id != folderId })
        }
    }

    private fun parse(json: String): List<AppFolder> = runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val o    = arr.getJSONObject(i)
            val apps = o.getJSONArray("apps")
            AppFolder(
                id           = o.getString("id"),
                name         = o.getString("name"),
                packageNames = (0 until apps.length()).map { apps.getString(it) }
            )
        }
    }.getOrDefault(emptyList())

    private fun serialize(folders: List<AppFolder>): String {
        val arr = JSONArray()
        folders.forEach { f ->
            val apps = JSONArray().apply { f.packageNames.forEach { put(it) } }
            arr.put(JSONObject().apply {
                put("id", f.id); put("name", f.name); put("apps", apps)
            })
        }
        return arr.toString()
    }
}
