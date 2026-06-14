package com.tien.tensor.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.tien.tensor.domain.model.UiPrefs
import java.util.Locale

/**
 * Runtime language override for the whole composition.
 *
 * Strategy: keep LocalContext pointing at the Activity at all times (avoids
 * ClassCastException and composition structure issues when the ContextImpl
 * returned by createConfigurationContext is used as LocalContext). Instead:
 *
 * 1. [SideEffect] – updates Activity.resources in-place so every
 *    stringResource call immediately resolves against the right locale.
 *    Also persists the choice to SharedPreferences so attachBaseContext
 *    can apply it synchronously on the next cold start.
 *
 * 2. [LocalConfiguration] – provides the locale-specific Configuration so
 *    stringResource composables invalidate and recompose when the locale changes.
 *
 * Command tokens are never routed through this mechanism; the parser is
 * locale-agnostic by design.
 */
@Composable
fun TensorLocale(languageTag: String, content: @Composable () -> Unit) {
    val context    = LocalContext.current
    val baseConfig = LocalConfiguration.current
    val prefs = remember { context.getSharedPreferences("tensor_locale", Context.MODE_PRIVATE) }

    val localCfg = remember(languageTag, baseConfig) {
        if (languageTag == UiPrefs.LANG_SYSTEM) {
            baseConfig
        } else {
            Configuration(baseConfig).apply { setLocale(Locale.forLanguageTag(languageTag)) }
        }
    }

    SideEffect {
        // Persist locale for next cold start (read synchronously in attachBaseContext)
        if (languageTag == UiPrefs.LANG_SYSTEM) {
            prefs.edit().remove("locale").apply()
        } else {
            prefs.edit().putString("locale", languageTag).apply()
        }
        // Apply locale to the Activity's Resources object so stringResource resolves
        // against the chosen locale without changing LocalContext (which must stay
        // as the Activity to keep ActivityResultContracts and other APIs stable).
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(localCfg, context.resources.displayMetrics)
    }

    CompositionLocalProvider(
        LocalConfiguration provides localCfg,
        content = content
    )
}
