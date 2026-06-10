package com.tien.tensor.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tien.tensor.domain.model.PinnedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class PinnedAppsDataSource(private val dataStore: DataStore<Preferences>) {

    private val pinnedKey = stringPreferencesKey("pinned_apps_v1")

    val pinnedApps: Flow<List<PinnedApp>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> parseApps(prefs[pinnedKey] ?: "[]") }

    suspend fun pinApp(packageName: String, appName: String): Boolean {
        val current = parseApps(dataStore.data.first()[pinnedKey] ?: "[]")
        if (current.any { it.packageName == packageName } || current.size >= MAX_PINNED) return false
        dataStore.edit { prefs ->
            prefs[pinnedKey] = serialize(current + PinnedApp(packageName, appName))
        }
        return true
    }

    suspend fun unpinApp(packageName: String) {
        dataStore.edit { prefs ->
            val updated = parseApps(prefs[pinnedKey] ?: "[]").filter { it.packageName != packageName }
            prefs[pinnedKey] = serialize(updated)
        }
    }

    suspend fun reorder(packageNames: List<String>) {
        dataStore.edit { prefs ->
            val current = parseApps(prefs[pinnedKey] ?: "[]").associateBy { it.packageName }
            val reordered = packageNames.mapNotNull { current[it] }
            prefs[pinnedKey] = serialize(reordered)
        }
    }

    private fun parseApps(json: String): List<PinnedApp> = runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            PinnedApp(o.getString("pkg"), o.getString("name"))
        }
    }.getOrDefault(emptyList())

    private fun serialize(apps: List<PinnedApp>): String {
        val arr = JSONArray()
        apps.forEach { a -> arr.put(JSONObject().apply { put("pkg", a.packageName); put("name", a.appName) }) }
        return arr.toString()
    }

    private companion object {
        const val MAX_PINNED = 5
    }
}
