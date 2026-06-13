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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
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
import com.tien.tensor.ui.component.LocalCursorBlink
import com.tien.tensor.ui.component.LocalTypingSpeed
import com.tien.tensor.ui.component.WallpaperLayer
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
                var hasBooted by rememberSaveable { mutableStateOf(false) }

                // ── Navigation back stack ────────────────────────────────
                // A plain saveable stack (no NavController) with HOME as the
                // permanent root. Both the system back gesture and every
                // visual [BACK] button run navigateBack(), so they can never
                // diverge — e.g. SECURITY → ARSENAL pops back to SECURITY.
                val backStack = rememberSaveable(
                    saver = listSaver(
                        save    = { stack -> stack.map { it.name } },
                        restore = { saved -> saved.map(AppDestination::valueOf).toMutableStateList() }
                    )
                ) { mutableStateListOf(AppDestination.HOME) }
                val destination = backStack.last()

                fun navigateTo(dest: AppDestination) {
                    when {
                        // HOME resets the stack: the launcher root never nests
                        dest == AppDestination.HOME -> backStack.removeRange(1, backStack.size)
                        // Ignore re-entrant taps to the current destination
                        backStack.last() != dest    -> backStack.add(dest)
                    }
                }
                fun navigateBack() {
                    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                }

                // On HOME the press is swallowed (a launcher never closes itself).
                // Screens with internal layers (arsenal detail, home overlays)
                // register their own enabled-when-needed BackHandler, which
                // takes precedence over this one.
                BackHandler { navigateBack() }

                // ── Spatial customization: signed margins vs system insets ──
                // The user margin is SIGNED. The effective edge padding is
                //   effective = max(0, systemInset + userMargin)
                // so positive values push the UI away from physical edges
                // (cases, curved glass, protectors) while negative values eat
                // into the inset/cutout padding until the UI sits flush
                // against the hardware edge. Clamping at 0 guarantees content
                // is never pushed off-screen.
                val density      = LocalDensity.current
                val topInsets    = WindowInsets.statusBars.union(WindowInsets.displayCutout)
                val bottomInsets = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                val insetTopDp    = with(density) { topInsets.getTop(this).toDp() }
                val insetBottomDp = with(density) { bottomInsets.getBottom(this).toDp() }
                val effectiveTop    = (insetTopDp + settingsState.uiPrefs.marginTopDp.dp).coerceAtLeast(0.dp)
                val effectiveBottom = (insetBottomDp + settingsState.uiPrefs.marginBottomDp.dp).coerceAtLeast(0.dp)

                CompositionLocalProvider(
                    LocalCursorBlink  provides settingsState.uiPrefs.cursorBlink,
                    LocalTypingSpeed  provides settingsState.uiPrefs.typingSpeedMs,
                ) {
                Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
                // Wallpaper sticker layer (transparent PNGs float over the
                // themed background); sits behind the status bar and every
                // destination. Styled via Settings → WALLPAPER or /wall.
                settingsState.wallpaperPath?.let { wp ->
                    WallpaperLayer(
                        path    = wp,
                        alpha   = settingsState.uiPrefs.wallpaperAlpha,
                        sizePct = settingsState.uiPrefs.wallpaperSizePct,
                        anchor  = settingsState.uiPrefs.wallpaperAnchor
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        // Horizontal cutouts (landscape punch-holes) are never
                        // user-overridable — content there is unreadable.
                        .windowInsetsPadding(topInsets.only(WindowInsetsSides.Horizontal))
                        // Vertical: inset + signed user margin, applied live
                        // (Settings → SPACING, or /margin t|b ±dp).
                        .padding(top = effectiveTop, bottom = effectiveBottom)
                ) {
                    if (!hasBooted) {
                        BootScreen(onBootComplete = { hasBooted = true })
                    } else {
                        TensorStatusBar(
                            viewModel = statusBarViewModel,
                            barSize   = settingsState.uiPrefs.statusBarSize,
                            showDate  = settingsState.uiPrefs.showDate,
                            opaque    = settingsState.uiPrefs.statusBarOpaque,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (destination) {
                                AppDestination.HOME ->
                                    LauncherScreen(onNavigate = { navigateTo(it) })
                                AppDestination.APP_LIST ->
                                    AppListScreen(onNavigateBack = { navigateBack() })
                                AppDestination.SETTINGS ->
                                    SettingsScreen(
                                        onNavigateBack = { navigateBack() },
                                        viewModel      = settingsViewModel
                                    )
                                AppDestination.SECURITY ->
                                    SecurityScreen(
                                        onNavigateBack = { navigateBack() },
                                        onOpenArsenal  = { navigateTo(AppDestination.ARSENAL) }
                                    )
                                AppDestination.ARSENAL ->
                                    ArsenalScreen(onNavigateBack = { navigateBack() })
                                AppDestination.INSIGHTS ->
                                    InsightsScreen(onNavigateBack = { navigateBack() })
                            }
                        }
                    }
                }
                } // Box
                } // CompositionLocalProvider
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
