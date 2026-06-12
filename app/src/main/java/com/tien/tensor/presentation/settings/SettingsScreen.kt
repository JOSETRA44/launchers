package com.tien.tensor.presentation.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tien.tensor.R
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.tensor.di.AppModule
import com.tien.tensor.domain.model.BarSize
import com.tien.tensor.domain.model.ThemeConfig
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.model.UiPrefs
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalPromptHeader
import com.tien.tensor.ui.component.TerminalSectionLabel
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppModule.settingsViewModelFactory())
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val colors  = LauncherTheme.colors
    val context = LocalContext.current

    val notifEnabled = remember(Unit) {
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = TensorSpacing.screenH)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(TensorSpacing.md))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TerminalPromptHeader(path = "settings")
            TerminalButton(label = stringResource(R.string.common_back), onClick = onNavigateBack)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // Theme section
        TerminalSectionLabel(label = stringResource(R.string.settings_theme))
        Spacer(Modifier.height(TensorSpacing.sm))
        state.availableThemes.forEach { theme ->
            ThemeOptionRow(theme = theme, isSelected = theme.id == state.selectedThemeId, onSelect = { viewModel.onThemeSelected(theme.id) })
            Spacer(Modifier.height(TensorSpacing.xs))
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // Display customization section
        TerminalSectionLabel(label = stringResource(R.string.settings_display))
        Spacer(Modifier.height(TensorSpacing.sm))

        OptionGroupRow(label = stringResource(R.string.settings_status_bar)) {
            BarSize.entries.forEach { size ->
                TerminalButton(
                    label   = size.displayName,
                    active  = state.uiPrefs.statusBarSize == size,
                    onClick = { viewModel.onBarSizeSelected(size) }
                )
            }
        }
        Spacer(Modifier.height(TensorSpacing.xs))
        OptionGroupRow(label = stringResource(R.string.settings_font_size)) {
            UiPrefs.FONT_SCALES.forEach { scale ->
                TerminalButton(
                    label   = "${(scale * 100).toInt()}%",
                    active  = state.uiPrefs.fontScale == scale,
                    onClick = { viewModel.onFontScaleSelected(scale) }
                )
            }
        }
        Spacer(Modifier.height(TensorSpacing.xs))
        OptionGroupRow(label = stringResource(R.string.settings_clock)) {
            TerminalButton(label = "12H", active = !state.uiPrefs.use24hClock, onClick = { viewModel.onClockFormatSelected(false) })
            TerminalButton(label = "24H", active = state.uiPrefs.use24hClock,  onClick = { viewModel.onClockFormatSelected(true) })
            TerminalButton(
                label   = if (state.uiPrefs.showClockSeconds) "SEC:ON" else "SEC:OFF",
                active  = state.uiPrefs.showClockSeconds,
                onClick = viewModel::onToggleClockSeconds
            )
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // Spatial customization: manual safety margins against physical edges
        TerminalSectionLabel(label = stringResource(R.string.settings_spacing))
        Spacer(Modifier.height(TensorSpacing.xs))
        Text(
            text  = stringResource(R.string.settings_spacing_hint),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface
        )
        Spacer(Modifier.height(TensorSpacing.sm))
        MarginStepperRow(
            label   = stringResource(R.string.settings_margin_top),
            valueDp = state.uiPrefs.marginTopDp,
            onStep  = { increase -> viewModel.onMarginStepped(top = true, increase = increase) }
        )
        Spacer(Modifier.height(TensorSpacing.xs))
        MarginStepperRow(
            label   = stringResource(R.string.settings_margin_bottom),
            valueDp = state.uiPrefs.marginBottomDp,
            onStep  = { increase -> viewModel.onMarginStepped(top = false, increase = increase) }
        )

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // Language section — UI labels only; terminal commands stay locale-agnostic
        TerminalSectionLabel(label = stringResource(R.string.settings_language))
        Spacer(Modifier.height(TensorSpacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
            TerminalButton(
                label   = stringResource(R.string.settings_lang_system),
                active  = state.uiPrefs.language == UiPrefs.LANG_SYSTEM,
                onClick = { viewModel.onLanguageSelected(UiPrefs.LANG_SYSTEM) },
                modifier = Modifier.weight(1f)
            )
            TerminalButton(label = "EN", active = state.uiPrefs.language == "en", onClick = { viewModel.onLanguageSelected("en") }, modifier = Modifier.weight(1f))
            TerminalButton(label = "ES", active = state.uiPrefs.language == "es", onClick = { viewModel.onLanguageSelected("es") }, modifier = Modifier.weight(1f))
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // Usage data section
        TerminalSectionLabel(label = stringResource(R.string.settings_usage_data))
        Spacer(Modifier.height(TensorSpacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.settings_launch_history), style = MaterialTheme.typography.bodySmall, color = colors.onBackground, modifier = Modifier.weight(1f))
            TerminalButton(
                label   = if (state.historyCleared) stringResource(R.string.settings_cleared) else stringResource(R.string.settings_clear_history),
                onClick = { if (!state.historyCleared) viewModel.onClearHistory() }
            )
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // Notifications section
        TerminalSectionLabel(label = stringResource(R.string.settings_notifications))
        Spacer(Modifier.height(TensorSpacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.settings_notif_badges), style = MaterialTheme.typography.bodySmall, color = colors.onBackground)
                Text(
                    text  = if (notifEnabled) stringResource(R.string.settings_enabled) else stringResource(R.string.settings_disabled),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (notifEnabled) colors.primary else colors.error
                )
            }
            if (!notifEnabled) {
                TerminalButton(
                    label   = stringResource(R.string.settings_grant_access),
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // System info section
        TerminalSectionLabel(label = stringResource(R.string.settings_system_info))
        Spacer(Modifier.height(TensorSpacing.sm))
        SystemInfoRow(key = "OS",       value = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        SystemInfoRow(key = "DEVICE",   value = "${Build.MANUFACTURER} ${Build.MODEL}".uppercase())
        SystemInfoRow(key = "ABI",      value = Build.SUPPORTED_ABIS.firstOrNull()?.uppercase() ?: "UNKNOWN")
        SystemInfoRow(key = "LAUNCHER", value = "TENSOR OS v1.0.0")

        Spacer(Modifier.height(TensorSpacing.md))
    }
}

/**
 * Stepper with a live signed gauge: `TOP MARGIN  [−] ██······ -16dp [+]`.
 * The gauge shows the magnitude; the color carries the sign — primary while
 * adding margin (pushing the UI inward), cursor color while negative
 * (compensating the system inset so the UI hugs the hardware edge).
 */
@Composable
private fun MarginStepperRow(label: String, valueDp: Int, onStep: (increase: Boolean) -> Unit) {
    val colors = LauncherTheme.colors
    val cells  = UiPrefs.MARGIN_MAX_DP / 8
    val filled = (kotlin.math.abs(valueDp) + 7) / 8
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = colors.terminalPrompt, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
            TerminalButton(label = "−", onClick = { onStep(false) })
            Text(
                text  = "█".repeat(filled) + "·".repeat(cells - filled),
                style = MaterialTheme.typography.labelSmall,
                color = if (valueDp < 0) colors.cursor else colors.primaryDim
            )
            Text(
                text  = "${valueDp}dp".padStart(5),
                style = MaterialTheme.typography.labelSmall,
                color = if (valueDp < 0) colors.cursor else colors.onBackground
            )
            TerminalButton(label = "+", onClick = { onStep(true) })
        }
    }
}

@Composable
private fun OptionGroupRow(label: String, options: @Composable () -> Unit) {
    val colors = LauncherTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = colors.terminalPrompt)
        Row(horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) { options() }
    }
}

@Composable
private fun ThemeOptionRow(theme: ThemeConfig, isSelected: Boolean, onSelect: () -> Unit) {
    val colors = LauncherTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isSelected) colors.primary else colors.border)
            .background(if (isSelected) colors.primaryDim else colors.background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onSelect)
            .padding(horizontal = TensorSpacing.md, vertical = TensorSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = if (isSelected) "> ${theme.name}" else "  ${theme.name}", style = MaterialTheme.typography.bodyMedium, color = if (isSelected) colors.primary else colors.onBackground)
        if (isSelected) Text(text = stringResource(R.string.settings_active), style = MaterialTheme.typography.labelSmall, color = colors.primary)
    }
}

@Composable
private fun SystemInfoRow(key: String, value: String) {
    val colors = LauncherTheme.colors
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = TensorSpacing.xxs), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.md)) {
        Text(text = key, style = MaterialTheme.typography.labelMedium, color = colors.terminalPrompt, modifier = Modifier.weight(0.35f))
        Text(text = ": $value", style = MaterialTheme.typography.bodySmall, color = colors.onBackground, modifier = Modifier.weight(0.65f))
    }
}
