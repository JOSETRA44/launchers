package com.tien.tensor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tien.tensor.di.AppModule
import com.tien.tensor.presentation.applist.AppListScreen
import com.tien.tensor.presentation.launcher.LauncherScreen
import com.tien.tensor.presentation.navigation.AppDestination
import com.tien.tensor.presentation.settings.SettingsScreen
import com.tien.tensor.presentation.settings.SettingsViewModel
import com.tien.tensor.ui.theme.LauncherTheme
import androidx.compose.foundation.layout.Box
import com.tien.tensor.ui.theme.TensorTheme

class MainActivity : ComponentActivity() {

    // SettingsViewModel lives here so the theme drives the entire composition tree
    private val settingsViewModel: SettingsViewModel by viewModels {
        AppModule.settingsViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Force light-on-dark system bar icons
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            TensorTheme(themeId = settingsState.selectedThemeId) {
                val colors = LauncherTheme.colors

                var destination by rememberSaveable { mutableStateOf(AppDestination.HOME) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                ) {
                    when (destination) {
                        AppDestination.HOME -> LauncherScreen(
                            onNavigate = { destination = it }
                        )
                        AppDestination.APP_LIST -> AppListScreen(
                            onNavigateBack = { destination = AppDestination.HOME }
                        )
                        AppDestination.SETTINGS -> SettingsScreen(
                            onNavigateBack = { destination = AppDestination.HOME },
                            viewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }
}
