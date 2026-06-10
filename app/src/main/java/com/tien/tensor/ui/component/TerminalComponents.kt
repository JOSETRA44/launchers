package com.tien.tensor.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

/** Thin horizontal terminal divider */
@Composable
fun TerminalDivider(modifier: Modifier = Modifier) {
    val colors = LauncherTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TensorSpacing.xs)
            .background(colors.border)
            .padding(vertical = 0.5.dp)
    )
}

/** Section label: `> RECENT` */
@Composable
fun TerminalSectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text = "> $label",
        color = LauncherTheme.colors.terminalPrompt,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
    )
}

/** Prompt header: `root@tensor:~/path$ ▮` */
@Composable
fun TerminalPromptHeader(path: String, modifier: Modifier = Modifier) {
    val colors = LauncherTheme.colors
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "root@tensor:~/$path",
            color = colors.terminalPrompt,
            style = MaterialTheme.typography.bodySmall
        )
        Text(text = "$ ", color = colors.primary, style = MaterialTheme.typography.bodySmall)
        BlinkingCursor(style = MaterialTheme.typography.bodySmall)
    }
}

/** Terminal-style search field: `$ query_` */
@Composable
fun TerminalSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "search_",
    onSearch: (() -> Unit)? = null
) {
    val colors = LauncherTheme.colors
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.primary),
        cursorBrush = SolidColor(colors.cursor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border)
                    .padding(horizontal = TensorSpacing.md, vertical = TensorSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$ ", color = colors.terminalPrompt, style = MaterialTheme.typography.bodyMedium)
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = colors.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    inner()
                }
            }
        }
    )
}

/** Terminal button: `[LABEL]` — no ripple, ASCII bracket style */
@Composable
fun TerminalButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false
) {
    val colors = LauncherTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .border(1.dp, if (active) colors.primary else colors.border)
            .background(if (active) colors.primary else colors.background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = TensorSpacing.md, vertical = TensorSpacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "[$label]",
            color = if (active) colors.background else colors.primary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
