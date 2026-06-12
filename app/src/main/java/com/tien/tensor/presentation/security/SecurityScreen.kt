package com.tien.tensor.presentation.security

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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tien.tensor.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.tensor.di.AppModule
import com.tien.tensor.domain.model.CheckStatus
import com.tien.tensor.domain.model.SecurityCheck
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalPromptHeader
import com.tien.tensor.ui.component.TerminalSearchField
import com.tien.tensor.ui.component.TerminalSectionLabel
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

private val PASSWORD_LENGTHS = listOf(12, 16, 24, 32)

@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    onOpenArsenal: () -> Unit = {},
    viewModel: SecurityViewModel = viewModel(factory = AppModule.securityViewModelFactory())
) {
    val state  by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LauncherTheme.colors

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
            TerminalPromptHeader(path = "security")
            TerminalButton(label = stringResource(R.string.common_back), onClick = onNavigateBack)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // Gateway to the modular security arsenal (/arsenal)
        TerminalButton(label = stringResource(R.string.security_open_arsenal), onClick = onOpenArsenal, modifier = Modifier.fillMaxWidth())

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // ── Device audit ──────────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TerminalSectionLabel(label = stringResource(R.string.security_device_audit) + if (state.report.warningCount > 0) " — ${state.report.warningCount} WARN" else "")
            TerminalButton(label = if (state.isLoading) "..." else stringResource(R.string.common_rescan), onClick = viewModel::refreshAudit)
        }
        Spacer(Modifier.height(TensorSpacing.sm))
        state.report.checks.forEach { check ->
            SecurityCheckRow(check)
            Spacer(Modifier.height(TensorSpacing.xs))
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // ── Password generator ────────────────────────────────────────────────
        TerminalSectionLabel(label = stringResource(R.string.security_password_generator))
        Spacer(Modifier.height(TensorSpacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
            PASSWORD_LENGTHS.forEach { len ->
                TerminalButton(
                    label   = "$len",
                    active  = state.passwordLength == len,
                    onClick = { viewModel.onPasswordLengthSelected(len) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(TensorSpacing.xs))
        TerminalButton(label = stringResource(R.string.security_generate), onClick = viewModel::onGeneratePassword, modifier = Modifier.fillMaxWidth())
        if (state.generatedPassword.isNotEmpty()) {
            Spacer(Modifier.height(TensorSpacing.xs))
            SelectionContainer {
                Text(text = "> ${state.generatedPassword}", style = MaterialTheme.typography.bodyMedium, color = colors.primary)
            }
            Text(text = stringResource(R.string.security_copy_hint), style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        // ── Hash tool ─────────────────────────────────────────────────────────
        TerminalSectionLabel(label = stringResource(R.string.security_hash_tool) + " — SHA-256")
        Spacer(Modifier.height(TensorSpacing.sm))
        TerminalSearchField(
            query         = state.hashInput,
            onQueryChange = viewModel::onHashInputChanged,
            placeholder   = "text_to_hash_",
            modifier      = Modifier.fillMaxWidth()
        )
        if (state.hashOutput.isNotEmpty()) {
            Spacer(Modifier.height(TensorSpacing.xs))
            SelectionContainer {
                Text(text = state.hashOutput, style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
        }

        Spacer(Modifier.height(TensorSpacing.md))
    }
}

@Composable
private fun SecurityCheckRow(check: SecurityCheck) {
    val colors = LauncherTheme.colors
    val (tag, tagColor) = when (check.status) {
        CheckStatus.PASS -> "[PASS]" to colors.primary
        CheckStatus.WARN -> "[WARN]" to colors.error
        CheckStatus.INFO -> "[INFO]" to colors.terminalPrompt
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(text = tag, style = MaterialTheme.typography.labelMedium, color = tagColor, modifier = Modifier.padding(end = TensorSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = check.label,  style = MaterialTheme.typography.bodyMedium, color = colors.onBackground)
            Text(text = check.detail, style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
        }
    }
}
