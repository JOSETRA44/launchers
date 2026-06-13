package com.tien.tensor.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.tien.tensor.domain.model.UiPrefs
import java.util.Locale

/**
 * Runtime language override for the whole composition.
 *
 * With the default ([UiPrefs.LANG_SYSTEM]) the native resource resolution
 * applies (values/, values-es/, … follow the system locale). When the user
 * picks a language explicitly, a configuration context with that locale is
 * provided so every `stringResource` call resolves against it — instantly,
 * no activity recreation.
 *
 * Command tokens never pass through here: the parser lives in the domain
 * layer and is locale-agnostic by design.
 */
@Composable
fun TensorLocale(languageTag: String, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val baseConfig  = LocalConfiguration.current
    // Always use CompositionLocalProvider so the composition tree structure is
    // stable regardless of the selected language. Switching conditionally between
    // a raw content() call and a wrapped one changes composition positions, which
    // causes every remembered state inside the tree to be disposed and recreated —
    // including ActivityResultContracts launchers — leading to a crash.
    val (localCtx, localCfg) = remember(languageTag, baseConfig) {
        if (languageTag == UiPrefs.LANG_SYSTEM) {
            baseContext to baseConfig
        } else {
            val cfg = Configuration(baseConfig).apply { setLocale(Locale.forLanguageTag(languageTag)) }
            baseContext.createConfigurationContext(cfg) to cfg
        }
    }
    CompositionLocalProvider(
        LocalContext provides localCtx,
        LocalConfiguration provides localCfg,
        content = content
    )
}
