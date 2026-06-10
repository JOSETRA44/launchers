package com.tien.tensor.presentation.launcher

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalSectionLabel
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

private data class CommandEntry(val syntax: String, val description: String)

private val COMMANDS = listOf(
    CommandEntry("/g <query>",        "Web search"),
    CommandEntry("/search <query>",   "Web search (alias)"),
    CommandEntry("/open <app>",       "Launch app by name"),
    CommandEntry("/info <app>",       "Open system app info"),
    CommandEntry("/pin <app>",        "Add app to dock"),
    CommandEntry("/unpin <app>",      "Remove app from dock"),
    CommandEntry("/theme <name>",     "Switch theme: dark / cyan / matrix"),
    CommandEntry("/settings",         "Open settings screen"),
    CommandEntry("/apps",             "Open full app list"),
    CommandEntry("/clean",            "Clear launch history"),
    CommandEntry("/help",             "Show this reference"),
)

@Composable
fun HelpOverlay(onDismiss: () -> Unit) {
    val colors = LauncherTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.97f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = TensorSpacing.screenH)
    ) {
        Spacer(Modifier.height(TensorSpacing.md))

        Row(
            modifier        = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TerminalSectionLabel(
                label    = "COMMAND REFERENCE",
                modifier = Modifier.weight(1f)
            )
            TerminalButton(label = "CLOSE", onClick = onDismiss)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            COMMANDS.forEach { cmd ->
                CommandRow(entry = cmd)
                Spacer(Modifier.height(TensorSpacing.xs))
            }

            TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

            Text(
                text  = "TIP: Enter any app name + tap Enter/Search to launch the top result.",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface
            )
            Spacer(Modifier.height(TensorSpacing.md))
        }
    }
}

@Composable
private fun CommandRow(entry: CommandEntry) {
    val colors = LauncherTheme.colors
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = TensorSpacing.xxs),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text     = entry.syntax,
            style    = MaterialTheme.typography.bodySmall,
            color    = colors.primary,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text     = entry.description,
            style    = MaterialTheme.typography.bodySmall,
            color    = colors.onBackground,
            modifier = Modifier.weight(0.58f)
        )
    }
}
