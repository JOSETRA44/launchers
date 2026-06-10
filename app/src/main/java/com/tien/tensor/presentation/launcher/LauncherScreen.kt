package com.tien.tensor.presentation.launcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.tensor.di.AppModule
import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.presentation.navigation.AppDestination
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalPromptHeader
import com.tien.tensor.ui.component.TerminalSearchField
import com.tien.tensor.ui.component.TerminalSectionLabel
import com.tien.tensor.ui.component.TypewriterText
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing

@Composable
fun LauncherScreen(
    onNavigate: (AppDestination) -> Unit,
    viewModel: LauncherViewModel = viewModel(factory = AppModule.launcherViewModelFactory())
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LauncherTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = TensorSpacing.screenH)
        ) {
            Spacer(Modifier.height(TensorSpacing.md))

            // Boot header with typewriter
            TypewriterText(
                fullText = "TENSOR OS [v1.0.0] — READY",
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary
            )
            Spacer(Modifier.height(TensorSpacing.xs))
            TerminalPromptHeader(path = "home")

            TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

            // Clock
            Text(
                text = state.currentTime,
                style = MaterialTheme.typography.displayMedium,
                color = colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = state.currentDate,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

            // Search field
            TerminalSearchField(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(TensorSpacing.sm))

            // Content: search results OR quick access
            if (state.searchQuery.isNotBlank()) {
                TerminalSectionLabel(label = "RESULTS (${state.searchResults.size})")
                Spacer(Modifier.height(TensorSpacing.xs))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.searchResults, key = { it.packageName }) { app ->
                        AppRowItem(
                            app = app,
                            onLaunch = { viewModel.onAppLaunch(app.packageName, app.appName) }
                        )
                    }
                }
            } else {
                TerminalSectionLabel(label = "QUICK ACCESS")
                Spacer(Modifier.height(TensorSpacing.xs))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.quickAccessApps, key = { it.packageName }) { app ->
                        AppRowItem(
                            app = app,
                            onLaunch = { viewModel.onAppLaunch(app.packageName, app.appName) }
                        )
                    }
                }
            }

            TerminalDivider()

            // Nav row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TensorSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(TensorSpacing.sm)
            ) {
                TerminalButton(
                    label = "ALL APPS",
                    onClick = { onNavigate(AppDestination.APP_LIST) },
                    modifier = Modifier.weight(1f)
                )
                TerminalButton(
                    label = "SETTINGS",
                    onClick = { onNavigate(AppDestination.SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Launching overlay
        AnimatedVisibility(
            visible = state.launchingAppName != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = "> LAUNCHING ${state.launchingAppName?.uppercase()}...",
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(TensorSpacing.md)
            )
        }
    }
}

@Composable
private fun AppRowItem(app: AppInfo, onLaunch: () -> Unit) {
    val colors = LauncherTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onLaunch)
            .padding(vertical = TensorSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "> ${app.appName}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "[LAUNCH]",
            style = MaterialTheme.typography.labelSmall,
            color = colors.primaryDim
        )
    }
}
