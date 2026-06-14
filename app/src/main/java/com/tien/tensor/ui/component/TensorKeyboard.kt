package com.tien.tensor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

/**
 * In-app terminal keyboard overlay — replaces the system IME so the launcher's
 * aesthetic is never broken by a foreign soft keyboard.
 *
 * Layout (bottom → top):
 *   Row 5 — symbols / space / dismiss
 *   Row 4 — shift · z..m · - · _
 *   Row 3 — a..l · . · ↵
 *   Row 2 — q..p · /
 *   Row 1 — 0..9 · ⌫
 *   Row 0 — quick command chips (scrollable)
 *
 * Shift toggles between lower-case and upper-case alpha keys.
 * All colour references go through [LauncherTheme.colors]; no hardcoded colours.
 */
@Composable
fun TensorKeyboard(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var shifted by remember { mutableStateOf(false) }
    val colors = LauncherTheme.colors

    val alpha = if (shifted) "QWERTYUIOPASDFGHJKL.ZXCVBNM"
    else "qwertyuiopasdfghjkl.zxcvbnm"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(1.dp, colors.border)
            .padding(vertical = TensorSpacing.xs, horizontal = TensorSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(TensorSpacing.xxs)
    ) {
        // Quick-command chips (scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xxs)
        ) {
            listOf("/g", "/open", "/pin", "/sec", "/cfg", "/stats", "/help", "/wall", "/lang").forEach { cmd ->
                KbChip(label = cmd, onClick = { onKey(cmd) })
            }
        }

        // Row 1 — digits + backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xxs)
        ) {
            "1234567890".forEach { c -> KbKey(label = c.toString(), onClick = { onKey(c.toString()) }) }
            KbKey(label = "⌫", weight = 1.5f, active = false, onClick = onBackspace)
        }

        // Row 2 — q..p + /
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xxs)
        ) {
            alpha.substring(0, 10).forEach { c -> KbKey(label = c.toString(), onClick = { onKey(c.toString()) }) }
            KbKey(label = "/", onClick = { onKey("/") })
        }

        // Row 3 — a..l + . + enter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xxs)
        ) {
            alpha.substring(10, 20).forEach { c -> KbKey(label = c.toString(), onClick = { onKey(c.toString()) }) }
            KbKey(label = "↵", weight = 1.5f, active = false, onClick = onEnter)
        }

        // Row 4 — shift + z..m + - + _ + shift
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xxs)
        ) {
            KbKey(label = "⇧", active = shifted, onClick = { shifted = !shifted })
            alpha.substring(20, 27).forEach { c -> KbKey(label = c.toString(), onClick = { onKey(c.toString()) }) }
            KbKey(label = "-", onClick = { onKey("-") })
            KbKey(label = "_", onClick = { onKey("_") })
            KbKey(label = "⇧", active = shifted, onClick = { shifted = !shifted })
        }

        // Row 5 — symbols + space + dismiss
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xxs)
        ) {
            listOf("@", "!", "|", ">", "#").forEach { s -> KbKey(label = s, onClick = { onKey(s) }) }
            KbKey(label = "SPC", weight = 3f, onClick = { onKey(" ") })
            KbKey(label = "?", onClick = { onKey("?") })
            KbKey(label = "×", active = false, onClick = onDismiss)
        }
    }
}

// ── Private helpers ──────────────────────────────────────────────────────────

@Composable
private fun RowScope.KbKey(
    label: String,
    weight: Float = 1f,
    active: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LauncherTheme.colors
    val is_ = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .weight(weight)
            .height(36.dp)
            .border(1.dp, if (active) colors.primary else colors.border)
            .background(if (active) colors.primary else colors.background)
            .clickable(interactionSource = is_, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) colors.background else colors.onBackground,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun KbChip(label: String, onClick: () -> Unit) {
    val colors = LauncherTheme.colors
    val is_ = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .border(1.dp, colors.primaryDim)
            .background(colors.surface)
            .clickable(interactionSource = is_, indication = null, onClick = onClick)
            .padding(horizontal = TensorSpacing.sm, vertical = TensorSpacing.xxs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = colors.primaryDim,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
