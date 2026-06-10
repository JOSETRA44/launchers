package com.tien.tensor.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tien.tensor.domain.model.ThemeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PreferencesDataSource(private val dataStore: DataStore<Preferences>) {

    private val themeKey = stringPreferencesKey("theme_id")

    val themeId: Flow<ThemeId> = dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val name = prefs[themeKey] ?: ThemeId.HACKER_DARK.name
            runCatching { ThemeId.valueOf(name) }.getOrDefault(ThemeId.HACKER_DARK)
        }

    suspend fun setThemeId(themeId: ThemeId) {
        dataStore.edit { prefs -> prefs[themeKey] = themeId.name }
    }
}
