package com.tien.tensor.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tien.tensor.data.launcher.AndroidAppLauncher
import com.tien.tensor.data.repository.AppRepositoryImpl
import com.tien.tensor.data.repository.AppUsageRepositoryImpl
import com.tien.tensor.data.repository.ThemeRepositoryImpl
import com.tien.tensor.data.source.AppDataSource
import com.tien.tensor.data.source.AppUsageDataSource
import com.tien.tensor.data.source.PreferencesDataSource
import com.tien.tensor.domain.port.AppLauncher
import com.tien.tensor.domain.port.AppRepository
import com.tien.tensor.domain.port.AppUsageRepository
import com.tien.tensor.domain.port.ThemeRepository
import com.tien.tensor.domain.usecase.ClearHistoryUseCase
import com.tien.tensor.domain.usecase.GetInstalledAppsUseCase
import com.tien.tensor.domain.usecase.GetSmartAppsUseCase
import com.tien.tensor.domain.usecase.GetThemeUseCase
import com.tien.tensor.domain.usecase.LaunchAppUseCase
import com.tien.tensor.domain.usecase.SearchAppsUseCase
import com.tien.tensor.domain.usecase.SetThemeUseCase
import com.tien.tensor.domain.usecase.TrackAppLaunchUseCase
import com.tien.tensor.presentation.applist.AppListViewModel
import com.tien.tensor.presentation.launcher.LauncherViewModel
import com.tien.tensor.presentation.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ── Infrastructure ────────────────────────────────────────────────────────

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val themeDataStore by lazy {
        PreferenceDataStoreFactory.create(
            scope = ioScope,
            produceFile = { appContext.preferencesDataStoreFile("tensor_prefs") }
        )
    }

    private val usageDataStore by lazy {
        PreferenceDataStoreFactory.create(
            scope = ioScope,
            produceFile = { appContext.preferencesDataStoreFile("tensor_usage") }
        )
    }

    private val appDataSource by lazy { AppDataSource(appContext.packageManager) }
    private val preferencesDataSource by lazy { PreferencesDataSource(themeDataStore) }
    private val appUsageDataSource by lazy { AppUsageDataSource(usageDataStore) }

    // ── Ports / Repositories ─────────────────────────────────────────────────

    private val appRepository: AppRepository by lazy {
        AppRepositoryImpl(appDataSource, appContext)
    }
    private val themeRepository: ThemeRepository by lazy {
        ThemeRepositoryImpl(preferencesDataSource)
    }
    private val appUsageRepository: AppUsageRepository by lazy {
        AppUsageRepositoryImpl(appUsageDataSource)
    }
    private val appLauncher: AppLauncher by lazy { AndroidAppLauncher(appContext) }

    // ── Use Cases ────────────────────────────────────────────────────────────

    val getInstalledAppsUseCase by lazy { GetInstalledAppsUseCase(appRepository) }
    val searchAppsUseCase by lazy { SearchAppsUseCase() }
    val launchAppUseCase by lazy { LaunchAppUseCase(appLauncher) }
    val getThemeUseCase by lazy { GetThemeUseCase(themeRepository) }
    val setThemeUseCase by lazy { SetThemeUseCase(themeRepository) }
    val trackAppLaunchUseCase by lazy { TrackAppLaunchUseCase(appUsageRepository) }
    val getSmartAppsUseCase by lazy { GetSmartAppsUseCase(appUsageRepository, appRepository) }
    val clearHistoryUseCase by lazy { ClearHistoryUseCase(appUsageRepository) }

    // ── ViewModel Factories ──────────────────────────────────────────────────

    fun launcherViewModelFactory() = factory {
        LauncherViewModel(
            getInstalledAppsUseCase,
            getSmartAppsUseCase,
            searchAppsUseCase,
            launchAppUseCase,
            trackAppLaunchUseCase
        )
    }

    fun appListViewModelFactory() = factory {
        AppListViewModel(
            getInstalledAppsUseCase,
            searchAppsUseCase,
            launchAppUseCase,
            trackAppLaunchUseCase
        )
    }

    fun settingsViewModelFactory() = factory {
        SettingsViewModel(getThemeUseCase, setThemeUseCase, clearHistoryUseCase)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : ViewModel> factory(create: () -> T): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <V : ViewModel> create(modelClass: Class<V>): V = create() as V
        }
}
