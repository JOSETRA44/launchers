package com.tien.tensor

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tien.tensor.di.AppModule
import com.tien.tensor.presentation.applist.AppListScreen
import com.tien.tensor.presentation.arsenal.ArsenalScreen
import com.tien.tensor.presentation.boot.BootScreen
import com.tien.tensor.presentation.insights.InsightsScreen
import com.tien.tensor.presentation.launcher.LauncherScreen
import com.tien.tensor.presentation.navigation.AppDestination
import com.tien.tensor.presentation.security.SecurityScreen
import com.tien.tensor.presentation.settings.SettingsScreen
import com.tien.tensor.presentation.settings.SettingsViewModel
import com.tien.tensor.presentation.statusbar.StatusBarViewModel
import com.tien.tensor.presentation.statusbar.TensorStatusBar
import com.tien.tensor.ui.TensorLocale
import com.tien.tensor.ui.theme.LauncherTheme
import com.tien.tensor.ui.theme.TensorTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    // SettingsViewModel lives here so the theme drives the entire composition tree
    private val settingsViewModel: SettingsViewModel by viewModels {
        AppModule.settingsViewModelFactory()
    }

    // Activity-scoped: the dynamic status bar is shared by every destination
    private val statusBarViewModel: StatusBarViewModel by viewModels {
        AppModule.statusBarViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyImmersiveMode()

        // Render into the cutout area instead of letterboxing while immersive;
        // the composition pads itself with the displayCutout insets.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            TensorLocale(languageTag = settingsState.uiPrefs.language) {
            TensorTheme(
                themeId   = settingsState.selectedThemeId,
                fontScale = settingsState.uiPrefs.fontScale
            ) {
                val colors = LauncherTheme.colors

                // Boot screen shown once per process lifecycle (survives rotation via rememberSaveable)
                var hasBooted   by rememberSaveable { mutableStateOf(false) }
                var destination by rememberSaveable { mutableStateOf(AppDestination.HOME) }

                // Prevent the launcher from being closed by back press.
                // On sub-screens, back returns to HOME; on HOME, the press is swallowed.
                BackHandler {
                    if (destination != AppDestination.HOME) destination = AppDestination.HOME
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                        // Keep the launcher's status bar clear of notches/punch-holes
                        // (display cutout) and of the system bar if it ever returns.
                        // Without this, devices with a camera cutout hide the bar.
                        .windowInsetsPadding(
                            WindowInsets.statusBars.union(WindowInsets.displayCutout)
                                .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                        )
                        // User-defined safety margins for cases/curved edges/protectors
                        // (Settings → SPACING, or /margin). Applied live.
                        .padding(
                            top    = settingsState.uiPrefs.marginTopDp.dp,
                            bottom = settingsState.uiPrefs.marginBottomDp.dp
                        )
                ) {
                    if (!hasBooted) {
                        BootScreen(onBootComplete = { hasBooted = true })
                    } else {
                        TensorStatusBar(viewModel = statusBarViewModel, barSize = settingsState.uiPrefs.statusBarSize)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (destination) {
                                AppDestination.HOME ->
                                    LauncherScreen(onNavigate = { destination = it })
                                AppDestination.APP_LIST ->
                                    AppListScreen(onNavigateBack = { destination = AppDestination.HOME })
                                AppDestination.SETTINGS ->
                                    SettingsScreen(
                                        onNavigateBack = { destination = AppDestination.HOME },
                                        viewModel      = settingsViewModel
                                    )
                                AppDestination.SECURITY ->
                                    SecurityScreen(
                                        onNavigateBack = { destination = AppDestination.HOME },
                                        onOpenArsenal  = { destination = AppDestination.ARSENAL }
                                    )
                                AppDestination.ARSENAL ->
                                    ArsenalScreen(onNavigateBack = { destination = AppDestination.HOME })
                                AppDestination.INSIGHTS ->
                                    InsightsScreen(onNavigateBack = { destination = AppDestination.HOME })
                            }
                        }
                    }
                }
            }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // The system restores its bars after dialogs/app switches — re-take the screen
        if (hasFocus) applyImmersiveMode()
    }

    /**
     * Full immersive mode: the launcher owns the whole screen. System bars are
     * hidden and only reappear transiently on an edge swipe, never pushing content.
     */
    private fun applyImmersiveMode() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
            isAppearanceLightStatusBars     = false
            isAppearanceLightNavigationBars = false
        }
    }
}
