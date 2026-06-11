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

private data class CmdEntry(val syntax: String, val description: String)

private val COMMANDS = listOf(
    CmdEntry("/g <query>",             "Web search"),
    CmdEntry("/open <app>",            "Launch app by name"),
    CmdEntry("/info <app>",            "Open system app info"),
    CmdEntry("/pin <app>",             "Add app to dock"),
    CmdEntry("/unpin <app>",           "Remove app from dock"),
    CmdEntry("/theme <name>",          "Switch theme: dark / cyan / matrix"),
    CmdEntry("/mkdir <name>",          "Create a folder"),
    CmdEntry("/group <folder> <app>",  "Add app to folder"),
    CmdEntry("/rmdir <folder>",        "Delete a folder"),
    CmdEntry("/folder <name>",         "Open folder overlay"),
    CmdEntry("/bar <s|m|l>",           "Status bar size"),
    CmdEntry("/font <90..125>",        "Font scale %"),
    CmdEntry("/clock <12|24>",         "Clock format"),
    CmdEntry("/settings",              "Open settings"),
    CmdEntry("/apps",                  "Open app list"),
    CmdEntry("/sec",                   "Security toolkit & device audit"),
    CmdEntry("/arsenal",               "Security arsenal — modular deep audits"),
    CmdEntry("/stats",                 "Screen time & step insights"),
    CmdEntry("/clean",                 "Clear launch history"),
    CmdEntry("/help",                  "Show this reference"),
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

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TerminalSectionLabel(label = "COMMAND REFERENCE", modifier = Modifier.weight(1f))
            TerminalButton(label = "CLOSE", onClick = onDismiss)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            COMMANDS.forEach { cmd ->
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = TensorSpacing.xxs),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = cmd.syntax,       style = MaterialTheme.typography.bodySmall, color = colors.primary,       modifier = Modifier.weight(0.48f))
                    Text(text = cmd.description,  style = MaterialTheme.typography.bodySmall, color = colors.onBackground,  modifier = Modifier.weight(0.52f))
                }
            }

            TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

            Text(
                text  = "Swipe up on the header area to open the app drawer.",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface
            )
            Spacer(Modifier.height(TensorSpacing.md))
        }
    }
}
