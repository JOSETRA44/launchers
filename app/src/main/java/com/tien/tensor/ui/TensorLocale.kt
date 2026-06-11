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
    if (languageTag == UiPrefs.LANG_SYSTEM) {
        content()
        return
    }
    val baseContext   = LocalContext.current
    val baseConfig    = LocalConfiguration.current
    val localized = remember(languageTag, baseContext, baseConfig) {
        val config = Configuration(baseConfig).apply { setLocale(Locale.forLanguageTag(languageTag)) }
        Pair(baseContext.createConfigurationContext(config), config)
    }
    CompositionLocalProvider(
        LocalContext provides localized.first,
        LocalConfiguration provides localized.second,
        content = content
    )
}
