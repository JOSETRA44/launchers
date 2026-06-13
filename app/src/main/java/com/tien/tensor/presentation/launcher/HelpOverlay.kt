package com.tien.tensor.presentation.launcher

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import com.tien.tensor.R
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalSectionLabel
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

/**
 * Command syntax stays locale-agnostic on purpose — only descriptions are
 * resolved through resources (values/, values-es/, …).
 */
private data class CmdEntry(val syntax: String, @StringRes val descriptionRes: Int)

private val COMMANDS = listOf(
    CmdEntry("/g <query>",            R.string.help_g),
    CmdEntry("/open <app>",           R.string.help_open),
    CmdEntry("/info <app>",           R.string.help_info),
    CmdEntry("/pin <app>",            R.string.help_pin),
    CmdEntry("/unpin <app>",          R.string.help_unpin),
    CmdEntry("/theme <name>",         R.string.help_theme),
    CmdEntry("/mkdir <name>",         R.string.help_mkdir),
    CmdEntry("/group <folder> <app>", R.string.help_group),
    CmdEntry("/rmdir <folder>",       R.string.help_rmdir),
    CmdEntry("/folder <name>",        R.string.help_folder),
    CmdEntry("/bar <s|m|l>",          R.string.help_bar),
    CmdEntry("/font <90..125>",       R.string.help_font),
    CmdEntry("/clock <12|24>",        R.string.help_clock),
    CmdEntry("/margin <t|b> <±dp>",   R.string.help_margin),
    CmdEntry("/lang <en|es|sys>",     R.string.help_lang),
    CmdEntry("/wall <off|alpha|size|pos>", R.string.help_wall),
    CmdEntry("/settings",             R.string.help_settings),
    CmdEntry("/apps",                 R.string.help_apps),
    CmdEntry("/sec",                  R.string.help_sec),
    CmdEntry("/arsenal",              R.string.help_arsenal),
    CmdEntry("/stats",                R.string.help_stats),
    CmdEntry("/clean",                R.string.help_clean),
    CmdEntry("/help",                 R.string.help_help),
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
            TerminalSectionLabel(label = stringResource(R.string.help_title), modifier = Modifier.weight(1f))
            TerminalButton(label = stringResource(R.string.common_close), onClick = onDismiss)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            COMMANDS.forEach { cmd ->
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = TensorSpacing.xxs),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = cmd.syntax, style = MaterialTheme.typography.bodySmall, color = colors.primary, modifier = Modifier.weight(0.48f))
                    Text(text = stringResource(cmd.descriptionRes), style = MaterialTheme.typography.bodySmall, color = colors.onBackground, modifier = Modifier.weight(0.52f))
                }
            }

            TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

            Text(
                text  = stringResource(R.string.help_swipe_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface
            )
            Spacer(Modifier.height(TensorSpacing.md))
        }
    }
}
