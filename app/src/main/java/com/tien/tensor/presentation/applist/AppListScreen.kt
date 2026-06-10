package com.tien.tensor.presentation.applist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tien.tensor.di.AppModule
import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.ui.component.TerminalButton
import com.tien.tensor.ui.component.TerminalDivider
import com.tien.tensor.ui.component.TerminalPromptHeader
import com.tien.tensor.ui.component.TerminalSearchField
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorSpacing
import kotlinx.coroutines.delay

@Composable
fun AppListScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppListViewModel = viewModel(factory = AppModule.appListViewModelFactory())
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LauncherTheme.colors

    // Group apps alphabetically
    val displayList = if (state.searchQuery.isBlank()) state.apps else state.filteredApps
    val grouped: Map<Char, List<AppInfo>> = displayList.groupBy {
        it.appName.firstOrNull()?.uppercaseChar() ?: '#'
    }

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
            TerminalPromptHeader(path = "apps")
            TerminalButton(label = "BACK", onClick = onNavigateBack)
        }

        TerminalDivider(Modifier.padding(vertical = TensorSpacing.sm))

        TerminalSearchField(
            query = state.searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            placeholder = "filter_",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(TensorSpacing.sm))

        Text(
            text = "${displayList.size} PACKAGES FOUND",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurface
        )

        Spacer(Modifier.height(TensorSpacing.xs))

        LazyColumn(modifier = Modifier.weight(1f)) {
            grouped.forEach { (letter, apps) ->
                stickyHeader(key = "h_$letter") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.background)
                            .padding(vertical = TensorSpacing.xs)
                    ) {
                        Text(
                            text = "[$letter]",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.terminalPrompt
                        )
                    }
                }
                itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                    StaggeredAppItem(
                        app = app,
                        index = index,
                        onLaunch = { viewModel.onAppLaunch(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StaggeredAppItem(app: AppInfo, index: Int, onLaunch: () -> Unit) {
    val colors = LauncherTheme.colors
    var visible by remember(app.packageName) { mutableStateOf(false) }

    LaunchedEffect(app.packageName) {
        delay(index.coerceAtMost(20) * 30L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + slideInHorizontally(tween(150)) { -it / 3 }
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, onClick = onLaunch)
                .padding(vertical = TensorSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "  > ${app.appName}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "[RUN]",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primaryDim
            )
        }
    }
}
