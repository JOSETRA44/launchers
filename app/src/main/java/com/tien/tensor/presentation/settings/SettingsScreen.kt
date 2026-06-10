package com.tien.tensor.presentation.settings

import android.os.Build
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.tensor.di.AppModule
import com.tien.tensor.domain.model.ThemeConfig
import com.tien.tensor.domain.model.ThemeId
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LauncherTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = TensorSpacing.screenH)
    ) {
        Spacer(Modifier.height(TensorSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TerminalPromptHeader(path = "settings")
            TerminalButton(label = "BACK", onClick = onNavigateBack)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        TerminalSectionLabel(label = "THEME")
        Spacer(Modifier.height(TensorSpacing.sm))

        state.availableThemes.forEach { theme ->
            ThemeOptionRow(
                theme = theme,
                isSelected = theme.id == state.selectedThemeId,
                onSelect = { viewModel.onThemeSelected(theme.id) }
            )
            Spacer(Modifier.height(TensorSpacing.xs))
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        TerminalSectionLabel(label = "USAGE DATA")
        Spacer(Modifier.height(TensorSpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Launch history (RECENT)",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
            TerminalButton(
                label = if (state.historyCleared) "CLEARED" else "CLEAR HISTORY",
                onClick = { if (!state.historyCleared) viewModel.onClearHistory() }
            )
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        TerminalSectionLabel(label = "SYSTEM INFO")
        Spacer(Modifier.height(TensorSpacing.sm))

        SystemInfoRow(key = "OS", value = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        SystemInfoRow(key = "DEVICE", value = "${Build.MANUFACTURER} ${Build.MODEL}".uppercase())
        SystemInfoRow(key = "LAUNCHER", value = "TENSOR OS v1.0.0")
    }
}

@Composable
private fun ThemeOptionRow(
    theme: ThemeConfig,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
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
        Text(
            text = if (isSelected) "> ${theme.name}" else "  ${theme.name}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) colors.primary else colors.onBackground
        )
        if (isSelected) {
            Text(
                text = "[ACTIVE]",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary
            )
        }
    }
}

@Composable
private fun SystemInfoRow(key: String, value: String) {
    val colors = LauncherTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TensorSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(TensorSpacing.md)
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelMedium,
            color = colors.terminalPrompt,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = ": $value",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onBackground,
            modifier = Modifier.weight(0.65f)
        )
    }
}
