package com.tien.tensor.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tien.tensor.domain.model.BarSize
import com.tien.tensor.domain.model.UiPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Persists UI customization (status bar size, font scale, clock format). */
class UiPrefsDataSource(private val dataStore: DataStore<Preferences>) {

    private val barSizeKey   = stringPreferencesKey("ui_bar_size")
    private val fontScaleKey = floatPreferencesKey("ui_font_scale")
    private val clock24Key   = booleanPreferencesKey("ui_clock_24h")
    private val secondsKey   = booleanPreferencesKey("ui_clock_seconds")

    val prefs: Flow<UiPrefs> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { p ->
            UiPrefs(
                statusBarSize    = runCatching { BarSize.valueOf(p[barSizeKey] ?: "") }.getOrDefault(BarSize.NORMAL),
                fontScale        = p[fontScaleKey] ?: 1.0f,
                use24hClock      = p[clock24Key] ?: true,
                showClockSeconds = p[secondsKey] ?: true
            )
        }

    suspend fun update(prefs: UiPrefs) {
        dataStore.edit { p ->
            p[barSizeKey]   = prefs.statusBarSize.name
            p[fontScaleKey] = prefs.fontScale
            p[clock24Key]   = prefs.use24hClock
            p[secondsKey]   = prefs.showClockSeconds
        }
    }
}
