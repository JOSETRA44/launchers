package com.tien.tensor.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tien.tensor.domain.model.BarSize
import com.tien.tensor.domain.model.UiPrefs
import com.tien.tensor.domain.model.WallpaperAnchor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Persists UI customization (status bar size, font scale, clock format). */
class UiPrefsDataSource(private val dataStore: DataStore<Preferences>) {

    private val barSizeKey   = stringPreferencesKey("ui_bar_size")
    private val fontScaleKey = floatPreferencesKey("ui_font_scale")
    private val clock24Key   = booleanPreferencesKey("ui_clock_24h")
    private val secondsKey   = booleanPreferencesKey("ui_clock_seconds")
    private val marginTopKey    = intPreferencesKey("ui_margin_top")
    private val marginBottomKey = intPreferencesKey("ui_margin_bottom")
    private val languageKey     = stringPreferencesKey("ui_language")
    private val wpAlphaKey      = floatPreferencesKey("ui_wp_alpha")
    private val wpSizeKey       = intPreferencesKey("ui_wp_size")
    private val wpAnchorKey     = stringPreferencesKey("ui_wp_anchor")

    val prefs: Flow<UiPrefs> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { p ->
            UiPrefs(
                statusBarSize    = runCatching { BarSize.valueOf(p[barSizeKey] ?: "") }.getOrDefault(BarSize.NORMAL),
                fontScale        = p[fontScaleKey] ?: 1.0f,
                use24hClock      = p[clock24Key] ?: true,
                showClockSeconds = p[secondsKey] ?: true,
                marginTopDp      = (p[marginTopKey] ?: 0).coerceIn(UiPrefs.MARGIN_MIN_DP, UiPrefs.MARGIN_MAX_DP),
                marginBottomDp   = (p[marginBottomKey] ?: 0).coerceIn(UiPrefs.MARGIN_MIN_DP, UiPrefs.MARGIN_MAX_DP),
                language         = p[languageKey] ?: UiPrefs.LANG_SYSTEM,
                wallpaperAlpha   = (p[wpAlphaKey] ?: 0.30f).coerceIn(0.05f, 1f),
                wallpaperSizePct = (p[wpSizeKey] ?: 60).coerceIn(10, 100),
                wallpaperAnchor  = runCatching { WallpaperAnchor.valueOf(p[wpAnchorKey] ?: "") }
                    .getOrDefault(WallpaperAnchor.BOTTOM_RIGHT)
            )
        }

    suspend fun update(prefs: UiPrefs) {
        dataStore.edit { p ->
            p[barSizeKey]   = prefs.statusBarSize.name
            p[fontScaleKey] = prefs.fontScale
            p[clock24Key]   = prefs.use24hClock
            p[secondsKey]   = prefs.showClockSeconds
            p[marginTopKey]    = prefs.marginTopDp.coerceIn(UiPrefs.MARGIN_MIN_DP, UiPrefs.MARGIN_MAX_DP)
            p[marginBottomKey] = prefs.marginBottomDp.coerceIn(UiPrefs.MARGIN_MIN_DP, UiPrefs.MARGIN_MAX_DP)
            p[languageKey]     = prefs.language
            p[wpAlphaKey]      = prefs.wallpaperAlpha.coerceIn(0.05f, 1f)
            p[wpSizeKey]       = prefs.wallpaperSizePct.coerceIn(10, 100)
            p[wpAnchorKey]     = prefs.wallpaperAnchor.name
        }
    }
}
