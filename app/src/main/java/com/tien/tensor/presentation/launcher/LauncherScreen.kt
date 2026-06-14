package com.tien.tensor.presentation.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.tien.tensor.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.tensor.di.AppModule
import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.domain.model.SmartApp
import com.tien.tensor.presentation.navigation.AppDestination
import com.tien.tensor.ui.component.TensorKeyboard
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalPromptHeader
import com.tien.tensor.ui.component.TerminalSearchField
import com.tien.tensor.ui.component.TerminalSectionLabel
import com.tien.tensor.ui.component.TypewriterText
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

private const val SWIPE_UP_THRESHOLD = -120f

@Composable
fun LauncherScreen(
    onNavigate: (AppDestination) -> Unit,
    viewModel: LauncherViewModel = viewModel(factory = AppModule.launcherViewModelFactory())
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showKeyboard by remember { mutableStateOf(false) }
    val kbCtrl = LocalSoftwareKeyboardController.current

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { dest -> onNavigate(dest) }
    }

    // System back peels home overlays one at a time — exactly the same state
    // change as their tap-to-dismiss action — instead of being swallowed by
    // the activity-level handler. Priority mirrors the render order below.
    BackHandler(enabled = state.showChargingOverlay || state.activeFolderId != null || state.showHelp || showKeyboard) {
        when {
            state.showChargingOverlay    -> viewModel.onDismissChargingOverlay()
            state.activeFolderId != null -> viewModel.onCloseFolderOverlay()
            state.showHelp               -> viewModel.onDismissHelp()
            else                         -> showKeyboard = false
        }
    }

    // Charging overlay takes precedence over everything
    if (state.showChargingOverlay) {
        ChargingOverlay(
            batteryPercent = state.systemStatus.batteryPercent,
            onDismiss      = viewModel::onDismissChargingOverlay
        )
        return
    }

    // Folder overlay takes precedence
    if (state.activeFolderId != null) {
        val folder = state.folders.firstOrNull { it.id == state.activeFolderId }
        if (folder != null) {
            FolderOverlay(
                folder             = folder,
                allApps            = state.allApps.associateBy { it.packageName },
                notificationCounts = state.notificationCounts,
                onLaunch           = { pkg, name -> viewModel.onAppLaunch(pkg, name) },
                onRemoveApp        = { pkg -> viewModel.onRemoveFromFolder(folder.id, pkg) },
                onClose            = viewModel::onCloseFolderOverlay
            )
            return
        }
    }

    if (state.showHelp) {
        HelpOverlay(onDismiss = viewModel::onDismissHelp)
        return
    }

    val colors = LauncherTheme.colors
    val swipeEnabled by rememberUpdatedState(state.searchQuery.isBlank())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var cumulativeDy = 0f
                detectVerticalDragGestures(
                    onDragStart  = { cumulativeDy = 0f },
                    onDragCancel = { cumulativeDy = 0f },
                    onDragEnd    = { if (swipeEnabled && cumulativeDy < SWIPE_UP_THRESHOLD) onNavigate(AppDestination.APP_LIST) },
                    onVerticalDrag = { _, dy -> cumulativeDy += dy }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = TensorSpacing.screenH)
        ) {
            Spacer(Modifier.height(TensorSpacing.sm))

            // Battery/network now live in the global TensorStatusBar (MainActivity)
            TypewriterText(fullText = "TENSOR OS [v1.0.0] — READY", style = MaterialTheme.typography.labelMedium, color = colors.primary)
            Spacer(Modifier.height(TensorSpacing.xs))
            TerminalPromptHeader(path = "home")

            TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

            // Clock
            Text(text = state.currentTime, style = MaterialTheme.typography.displayMedium, color = colors.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(text = state.currentDate, style = MaterialTheme.typography.bodySmall,    color = colors.onSurface,    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

            TerminalSearchField(query = state.searchQuery, onQueryChange = viewModel::onSearchQueryChanged, onSearch = viewModel::onSearchSubmit, modifier = Modifier.fillMaxWidth())

            // Command output
            AnimatedVisibility(visible = state.commandOutput != null) {
                Text(text = state.commandOutput ?: "", style = MaterialTheme.typography.labelSmall, color = colors.terminalPrompt, modifier = Modifier.fillMaxWidth().padding(top = TensorSpacing.xxs))
            }

            // Command history chips
            if (state.commandHistory.isNotEmpty() && state.searchQuery.isBlank()) {
                Spacer(Modifier.height(TensorSpacing.xs))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
                    state.commandHistory.take(5).forEach { cmd ->
                        TerminalButton(label = cmd, onClick = { viewModel.onHistoryTap(cmd) })
                    }
                }
            }

            Spacer(Modifier.height(TensorSpacing.sm))

            // App list section
            if (state.searchQuery.isNotBlank()) {
                TerminalSectionLabel(label = stringResource(R.string.home_results, state.searchResults.size))
                Spacer(Modifier.height(TensorSpacing.xs))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.searchResults, key = { it.packageName }) { app ->
                        AppRowItem(app = app, notifCount = state.notificationCounts[app.packageName] ?: 0, onLaunch = { viewModel.onAppLaunch(app.packageName, app.appName) })
                    }
                }
            } else {
                val showSmart = state.smartApps.isNotEmpty()
                TerminalSectionLabel(label = if (showSmart) stringResource(R.string.home_recent) else stringResource(R.string.home_apps))
                Spacer(Modifier.height(TensorSpacing.xs))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (showSmart) {
                        items(state.smartApps, key = { it.packageName }) { app ->
                            SmartAppRow(app = app, notifCount = state.notificationCounts[app.packageName] ?: 0, onLaunch = { viewModel.onAppLaunch(app.packageName, app.appName) })
                        }
                    } else {
                        items(state.allApps.take(5), key = { it.packageName }) { app ->
                            AppRowItem(app = app, notifCount = state.notificationCounts[app.packageName] ?: 0, onLaunch = { viewModel.onAppLaunch(app.packageName, app.appName) })
                        }
                    }
                }
            }

            TerminalDivider()

            // Folders
            if (state.folders.isNotEmpty()) {
                Spacer(Modifier.height(TensorSpacing.xs))
                TerminalSectionLabel(label = stringResource(R.string.home_folders))
                Spacer(Modifier.height(TensorSpacing.xs))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
                    state.folders.forEach { folder ->
                        TerminalButton(
                            label   = "${folder.name.take(8)}/${folder.packageNames.size}".uppercase(),
                            onClick = { viewModel.onOpenFolder(folder.id) }
                        )
                    }
                }
                Spacer(Modifier.height(TensorSpacing.xs))
            }

            // Dock
            if (state.pinnedApps.isNotEmpty()) {
                if (state.folders.isEmpty()) Spacer(Modifier.height(TensorSpacing.xs))
                TerminalSectionLabel(label = stringResource(R.string.home_dock))
                Spacer(Modifier.height(TensorSpacing.xs))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
                    state.pinnedApps.forEach { app ->
                        val count = state.notificationCounts[app.packageName] ?: 0
                        val label = if (count > 0) "${app.appName.take(7)}($count)".uppercase() else app.appName.take(10).uppercase()
                        TerminalButton(label = label, onClick = { viewModel.onAppLaunch(app.packageName, app.appName) })
                    }
                }
                Spacer(Modifier.height(TensorSpacing.xs))
            }

            // Nav row
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = TensorSpacing.sm), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
                TerminalButton(label = "APPS",  onClick = { onNavigate(AppDestination.APP_LIST) }, modifier = Modifier.weight(1f))
                TerminalButton(label = "SEC",   onClick = { onNavigate(AppDestination.SECURITY) }, modifier = Modifier.weight(1f))
                TerminalButton(label = "STATS", onClick = { onNavigate(AppDestination.INSIGHTS) }, modifier = Modifier.weight(1f))
                TerminalButton(label = "CFG",   onClick = { onNavigate(AppDestination.SETTINGS) }, modifier = Modifier.weight(1f))
                TerminalButton(
                    label  = "⌨",
                    active = showKeyboard,
                    onClick = {
                        showKeyboard = !showKeyboard
                        if (showKeyboard) kbCtrl?.hide()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        AnimatedVisibility(visible = state.launchingAppName != null, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.BottomCenter)) {
            Text(text = stringResource(R.string.home_launching, state.launchingAppName?.uppercase() ?: ""), style = MaterialTheme.typography.labelMedium, color = colors.primary,
                modifier = Modifier.fillMaxWidth().background(colors.surface).padding(TensorSpacing.md))
        }

        AnimatedVisibility(
            visible  = showKeyboard,
            enter    = slideInVertically { it },
            exit     = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            TensorKeyboard(
                onKey       = { char -> viewModel.onSearchQueryChanged(state.searchQuery + char) },
                onBackspace = { if (state.searchQuery.isNotEmpty()) viewModel.onSearchQueryChanged(state.searchQuery.dropLast(1)) },
                onEnter     = { viewModel.onSearchSubmit(); showKeyboard = false },
                onDismiss   = { showKeyboard = false }
            )
        }
    }
}

@Composable
private fun SmartAppRow(app: SmartApp, notifCount: Int, onLaunch: () -> Unit) {
    val colors = LauncherTheme.colors; val is_ = remember { MutableInteractionSource() }
    Row(modifier = Modifier.fillMaxWidth().clickable(interactionSource = is_, indication = null, onClick = onLaunch).padding(vertical = TensorSpacing.xs), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "> ${app.appName}", style = MaterialTheme.typography.bodyMedium, color = colors.onBackground)
            if (notifCount > 0) Text(text = "[$notifCount]", style = MaterialTheme.typography.labelSmall, color = colors.error)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs)) {
            Text(text = "x${app.launchCount}", style = MaterialTheme.typography.labelSmall, color = colors.terminalPrompt)
            Text(text = "[${timeAgo(app.lastLaunchAt)}]", style = MaterialTheme.typography.labelSmall, color = colors.primaryDim)
        }
    }
}

@Composable
private fun AppRowItem(app: AppInfo, notifCount: Int, onLaunch: () -> Unit) {
    val colors = LauncherTheme.colors; val is_ = remember { MutableInteractionSource() }
    Row(modifier = Modifier.fillMaxWidth().clickable(interactionSource = is_, indication = null, onClick = onLaunch).padding(vertical = TensorSpacing.xs), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(TensorSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "> ${app.appName}", style = MaterialTheme.typography.bodyMedium, color = colors.onBackground)
            if (notifCount > 0) Text(text = "[$notifCount]", style = MaterialTheme.typography.labelSmall, color = colors.error)
        }
        Text(text = "[LAUNCH]", style = MaterialTheme.typography.labelSmall, color = colors.primaryDim)
    }
}

private fun timeAgo(ts: Long): String {
    val d = System.currentTimeMillis() - ts
    return when { d < 60_000 -> "now"; d < 3_600_000 -> "${d/60_000}m"; d < 86_400_000 -> "${d/3_600_000}h"; else -> "${d/86_400_000}d" }
}
