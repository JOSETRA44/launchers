package com.tien.tensor.presentation.launcher

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tien.tensor.domain.model.AppFolder
import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalSectionLabel
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

@Composable
fun FolderOverlay(
    folder: AppFolder,
    allApps: Map<String, AppInfo>,
    notificationCounts: Map<String, Int>,
    onLaunch: (packageName: String, appName: String) -> Unit,
    onRemoveApp: (packageName: String) -> Unit,
    onClose: () -> Unit
) {
    val colors = LauncherTheme.colors
    val apps   = folder.packageNames.mapNotNull { pkg -> allApps[pkg] }

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
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                TerminalSectionLabel(label = "FOLDER: ${folder.name.uppercase()}")
                Text(
                    text  = "${apps.size} apps",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface
                )
            }
            TerminalButton(label = "CLOSE", onClick = onClose)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        if (apps.isEmpty()) {
            Text(
                text  = "Empty. Use /group ${folder.name.lowercase()} <app> to add apps.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(apps, key = { it.packageName }) { app ->
                    FolderAppRow(
                        app                = app,
                        notifCount         = notificationCounts[app.packageName] ?: 0,
                        onLaunch           = { onLaunch(app.packageName, app.appName) },
                        onRemove           = { onRemoveApp(app.packageName) }
                    )
                }
            }
        }

        Spacer(Modifier.height(TensorSpacing.sm))
        Text(
            text  = "Tip: /group ${folder.name.lowercase()} <app>  |  /rmdir ${folder.name.lowercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface
        )
        Spacer(Modifier.height(TensorSpacing.sm))
    }
}

@Composable
private fun FolderAppRow(
    app: AppInfo,
    notifCount: Int,
    onLaunch: () -> Unit,
    onRemove: () -> Unit
) {
    val colors            = LauncherTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onLaunch)
            .padding(vertical = TensorSpacing.xs),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier          = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)
        ) {
            Text(text = "> ${app.appName}", style = MaterialTheme.typography.bodyMedium, color = colors.onBackground)
            if (notifCount > 0) {
                Text(text = "[$notifCount]", style = MaterialTheme.typography.labelSmall, color = colors.error)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
            Text(text = "[RUN]", style = MaterialTheme.typography.labelSmall, color = colors.primaryDim)
            val rmInteraction = remember { MutableInteractionSource() }
            Text(
                text     = "[RM]",
                style    = MaterialTheme.typography.labelSmall,
                color    = colors.error,
                modifier = Modifier.clickable(interactionSource = rmInteraction, indication = null, onClick = onRemove)
            )
        }
    }
}
