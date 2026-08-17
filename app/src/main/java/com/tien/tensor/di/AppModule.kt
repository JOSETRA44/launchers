package com.tien.tensor.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tien.tensor.data.launcher.AndroidAppLauncher
import com.tien.tensor.data.launcher.AppInfoLauncherImpl
import com.tien.tensor.data.launcher.WebSearchLauncherImpl
import com.tien.tensor.data.repository.AppRepositoryImpl
import com.tien.tensor.data.repository.AppUsageRepositoryImpl
import com.tien.tensor.data.repository.FolderRepositoryImpl
import com.tien.tensor.data.repository.NotificationRepositoryImpl
import com.tien.tensor.data.repository.PinnedAppsRepositoryImpl
import com.tien.tensor.data.repository.SecurityRepositoryImpl
import com.tien.tensor.data.repository.StepCounterRepositoryImpl
import com.tien.tensor.data.repository.SystemStatusRepositoryImpl
import com.tien.tensor.data.repository.ThemeRepositoryImpl
import com.tien.tensor.data.repository.UiPrefsRepositoryImpl
import com.tien.tensor.data.repository.UsageStatsRepositoryImpl
import com.tien.tensor.data.repository.WallpaperRepositoryImpl
import com.tien.tensor.data.source.AppDataSource
import com.tien.tensor.data.source.AppUsageDataSource
import com.tien.tensor.data.source.FolderDataSource
import com.tien.tensor.data.source.PinnedAppsDataSource
import com.tien.tensor.data.source.PreferencesDataSource
import com.tien.tensor.data.source.SecurityDataSource
import com.tien.tensor.data.source.StepCounterDataSource
import com.tien.tensor.data.source.SystemStatusDataSource
import com.tien.tensor.data.source.UiPrefsDataSource
import com.tien.tensor.data.source.WallpaperDataSource
import com.tien.tensor.data.arsenal.AccessControlModule
import com.tien.tensor.data.arsenal.AppRiskModule
import com.tien.tensor.data.arsenal.ArsenalRegistryImpl
import com.tien.tensor.data.arsenal.BluetoothScannerModule
import com.tien.tensor.data.arsenal.DeviceIntegrityModule
import com.tien.tensor.data.arsenal.NetworkIntelModule
import com.tien.tensor.data.arsenal.PortScanModule
import com.tien.tensor.data.arsenal.RuntimeTelemetryModule
import com.tien.tensor.data.arsenal.SslInspectorModule
import com.tien.tensor.data.arsenal.TrustStoreModule
import com.tien.tensor.data.arsenal.WifiScannerModule
import com.tien.tensor.data.source.UsageStatsDataSource
import com.tien.tensor.domain.port.AppInfoLauncher
import com.tien.tensor.domain.port.AppLauncher
import com.tien.tensor.domain.port.ArsenalRegistry
import com.tien.tensor.domain.port.AppRepository
import com.tien.tensor.domain.port.AppUsageRepository
import com.tien.tensor.domain.port.FolderRepository
import com.tien.tensor.domain.port.NotificationRepository
import com.tien.tensor.domain.port.PinnedAppsRepository
import com.tien.tensor.domain.port.SecurityRepository
import com.tien.tensor.domain.port.StepCounterRepository
import com.tien.tensor.domain.port.SystemStatusRepository
import com.tien.tensor.domain.port.ThemeRepository
import com.tien.tensor.domain.port.UiPrefsRepository
import com.tien.tensor.domain.port.UsageStatsRepository
import com.tien.tensor.domain.port.WallpaperRepository
import com.tien.tensor.domain.port.WebSearchLauncher
import com.tien.tensor.domain.usecase.AddToFolderUseCase
import com.tien.tensor.domain.usecase.ClearHistoryUseCase
import com.tien.tensor.domain.usecase.ClearWallpaperUseCase
import com.tien.tensor.domain.usecase.ObserveWallpaperUseCase
import com.tien.tensor.domain.usecase.SetWallpaperUseCase
import com.tien.tensor.domain.usecase.CreateFolderUseCase
import com.tien.tensor.domain.usecase.DeleteFolderUseCase
import com.tien.tensor.domain.usecase.GeneratePasswordUseCase
import com.tien.tensor.domain.usecase.GetArsenalModulesUseCase
import com.tien.tensor.domain.usecase.ObserveArsenalModuleUseCase
import com.tien.tensor.domain.usecase.GetFoldersUseCase
import com.tien.tensor.domain.usecase.GetInstalledAppsUseCase
import com.tien.tensor.domain.usecase.GetNotificationCountsUseCase
import com.tien.tensor.domain.usecase.GetPinnedAppsUseCase
import com.tien.tensor.domain.usecase.GetSecurityReportUseCase
import com.tien.tensor.domain.usecase.GetSmartAppsUseCase
import com.tien.tensor.domain.usecase.GetStepsUseCase
import com.tien.tensor.domain.usecase.GetSystemStatusUseCase
import com.tien.tensor.domain.usecase.GetThemeUseCase
import com.tien.tensor.domain.usecase.GetUiPrefsUseCase
import com.tien.tensor.domain.usecase.UpdateUiPrefsUseCase
import com.tien.tensor.domain.usecase.GetUsageStatsUseCase
import com.tien.tensor.domain.usecase.HashTextUseCase
import com.tien.tensor.domain.usecase.LaunchAppUseCase
import com.tien.tensor.domain.usecase.ParseCommandUseCase
import com.tien.tensor.domain.usecase.PinAppUseCase
import com.tien.tensor.domain.usecase.RemoveFromFolderUseCase
import com.tien.tensor.domain.usecase.SearchAppsUseCase
import com.tien.tensor.domain.usecase.SetThemeUseCase
import com.tien.tensor.domain.usecase.TrackAppLaunchUseCase
import com.tien.tensor.domain.usecase.UnpinAppUseCase
import com.tien.tensor.presentation.applist.AppListViewModel
import com.tien.tensor.presentation.arsenal.ArsenalViewModel
import com.tien.tensor.presentation.insights.InsightsViewModel
import com.tien.tensor.presentation.launcher.LauncherViewModel
import com.tien.tensor.presentation.security.SecurityViewModel
import com.tien.tensor.presentation.settings.SettingsViewModel
import com.tien.tensor.presentation.statusbar.StatusBarViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppModule {

    private lateinit var appContext: Context

    fun init(context: Context) { appContext = context.applicationContext }

    // ── Infra ─────────────────────────────────────────────────────────────────

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun dataStore(name: String) = PreferenceDataStoreFactory.create(
        scope = ioScope, produceFile = { appContext.preferencesDataStoreFile(name) }
    )

    private val themeDataStore   by lazy { dataStore("tensor_prefs")  }
    private val usageDataStore   by lazy { dataStore("tensor_usage")  }
    private val pinnedDataStore  by lazy { dataStore("tensor_pinned") }
    private val folderDataStore  by lazy { dataStore("tensor_folders") }
    private val stepsDataStore   by lazy { dataStore("tensor_steps") }

    private val appDataSource         by lazy { AppDataSource(appContext.packageManager) }
    private val preferencesDataSource by lazy { PreferencesDataSource(themeDataStore) }
    private val appUsageDataSource    by lazy { AppUsageDataSource(usageDataStore) }
    private val pinnedAppsDataSource  by lazy { PinnedAppsDataSource(pinnedDataStore) }
    private val folderDataSource      by lazy { FolderDataSource(folderDataStore) }
    private val systemStatusDataSource by lazy { SystemStatusDataSource(appContext) }
    private val uiPrefsDataSource      by lazy { UiPrefsDataSource(themeDataStore) }
    private val wallpaperDataSource    by lazy { WallpaperDataSource(appContext) }
    private val securityDataSource    by lazy { SecurityDataSource(appContext) }
    private val usageStatsDataSource  by lazy { UsageStatsDataSource(appContext) }
    private val stepCounterDataSource by lazy { StepCounterDataSource(appContext, stepsDataStore) }

    // ── Repositories ──────────────────────────────────────────────────────────

    private val appRepository: AppRepository           by lazy { AppRepositoryImpl(appDataSource, appContext) }
    private val themeRepository: ThemeRepository       by lazy { ThemeRepositoryImpl(preferencesDataSource) }
    private val uiPrefsRepository: UiPrefsRepository   by lazy { UiPrefsRepositoryImpl(uiPrefsDataSource) }
    private val wallpaperRepository: WallpaperRepository by lazy { WallpaperRepositoryImpl(wallpaperDataSource) }
    private val appUsageRepository: AppUsageRepository by lazy { AppUsageRepositoryImpl(appUsageDataSource) }
    private val pinnedAppsRepository: PinnedAppsRepository by lazy { PinnedAppsRepositoryImpl(pinnedAppsDataSource) }
    private val folderRepository: FolderRepository     by lazy { FolderRepositoryImpl(folderDataSource) }
    private val systemStatusRepository: SystemStatusRepository by lazy { SystemStatusRepositoryImpl(systemStatusDataSource) }
    private val notificationRepository: NotificationRepository by lazy { NotificationRepositoryImpl() }
    private val securityRepository: SecurityRepository         by lazy { SecurityRepositoryImpl(securityDataSource) }
    private val usageStatsRepository: UsageStatsRepository     by lazy { UsageStatsRepositoryImpl(usageStatsDataSource) }
    private val stepCounterRepository: StepCounterRepository   by lazy { StepCounterRepositoryImpl(stepCounterDataSource) }

    // Security Arsenal plugin registry — add new SecurityModule implementations here
    private val arsenalRegistry: ArsenalRegistry by lazy {
        ArsenalRegistryImpl(
            listOf(
                DeviceIntegrityModule(appContext),
                AccessControlModule(appContext),
                AppRiskModule(appContext),
                TrustStoreModule(),
                NetworkIntelModule(appContext),
                RuntimeTelemetryModule(appContext),
                PortScanModule(appContext),
                WifiScannerModule(appContext),
                BluetoothScannerModule(appContext),
                SslInspectorModule()
            )
        )
    }

    // ── Ports ─────────────────────────────────────────────────────────────────

    private val appLauncher: AppLauncher             by lazy { AndroidAppLauncher(appContext) }
    private val appInfoLauncher: AppInfoLauncher     by lazy { AppInfoLauncherImpl(appContext) }
    private val webSearchLauncher: WebSearchLauncher by lazy { WebSearchLauncherImpl(appContext) }

    // ── Use Cases ─────────────────────────────────────────────────────────────

    private val getInstalledAppsUseCase   by lazy { GetInstalledAppsUseCase(appRepository) }
    private val searchAppsUseCase         by lazy { SearchAppsUseCase() }
    private val launchAppUseCase          by lazy { LaunchAppUseCase(appLauncher) }
    private val getThemeUseCase           by lazy { GetThemeUseCase(themeRepository) }
    val setThemeUseCase                   by lazy { SetThemeUseCase(themeRepository) }
    private val getUiPrefsUseCase         by lazy { GetUiPrefsUseCase(uiPrefsRepository) }
    private val updateUiPrefsUseCase      by lazy { UpdateUiPrefsUseCase(uiPrefsRepository) }
    private val observeWallpaperUseCase   by lazy { ObserveWallpaperUseCase(wallpaperRepository) }
    private val setWallpaperUseCase       by lazy { SetWallpaperUseCase(wallpaperRepository) }
    private val clearWallpaperUseCase     by lazy { ClearWallpaperUseCase(wallpaperRepository) }
    private val trackAppLaunchUseCase     by lazy { TrackAppLaunchUseCase(appUsageRepository) }
    private val getSmartAppsUseCase       by lazy { GetSmartAppsUseCase(appUsageRepository, appRepository) }
    private val clearHistoryUseCase       by lazy { ClearHistoryUseCase(appUsageRepository) }
    private val getPinnedAppsUseCase      by lazy { GetPinnedAppsUseCase(pinnedAppsRepository) }
    private val pinAppUseCase             by lazy { PinAppUseCase(pinnedAppsRepository) }
    private val unpinAppUseCase           by lazy { UnpinAppUseCase(pinnedAppsRepository) }
    private val parseCommandUseCase       by lazy { ParseCommandUseCase() }
    private val getFoldersUseCase         by lazy { GetFoldersUseCase(folderRepository) }
    private val createFolderUseCase       by lazy { CreateFolderUseCase(folderRepository) }
    private val addToFolderUseCase        by lazy { AddToFolderUseCase(folderRepository) }
    private val removeFromFolderUseCase   by lazy { RemoveFromFolderUseCase(folderRepository) }
    private val deleteFolderUseCase       by lazy { DeleteFolderUseCase(folderRepository) }
    private val getSystemStatusUseCase    by lazy { GetSystemStatusUseCase(systemStatusRepository) }
    private val getNotificationCountsUseCase by lazy { GetNotificationCountsUseCase(notificationRepository) }
    private val getSecurityReportUseCase  by lazy { GetSecurityReportUseCase(securityRepository) }
    private val generatePasswordUseCase   by lazy { GeneratePasswordUseCase() }
    private val hashTextUseCase           by lazy { HashTextUseCase() }
    private val getUsageStatsUseCase      by lazy { GetUsageStatsUseCase(usageStatsRepository) }
    private val getStepsUseCase           by lazy { GetStepsUseCase(stepCounterRepository) }
    private val getArsenalModulesUseCase  by lazy { GetArsenalModulesUseCase(arsenalRegistry) }
    private val observeArsenalModuleUseCase by lazy { ObserveArsenalModuleUseCase(arsenalRegistry) }

    // ── ViewModel Factories ───────────────────────────────────────────────────

    fun launcherViewModelFactory() = factory {
        LauncherViewModel(
            getInstalledAppsUseCase      = getInstalledAppsUseCase,
            getSmartAppsUseCase          = getSmartAppsUseCase,
            getPinnedAppsUseCase         = getPinnedAppsUseCase,
            getFoldersUseCase            = getFoldersUseCase,
            searchAppsUseCase            = searchAppsUseCase,
            launchAppUseCase             = launchAppUseCase,
            trackAppLaunchUseCase        = trackAppLaunchUseCase,
            clearHistoryUseCase          = clearHistoryUseCase,
            pinAppUseCase                = pinAppUseCase,
            unpinAppUseCase              = unpinAppUseCase,
            createFolderUseCase          = createFolderUseCase,
            addToFolderUseCase           = addToFolderUseCase,
            removeFromFolderUseCase      = removeFromFolderUseCase,
            deleteFolderUseCase          = deleteFolderUseCase,
            parseCommandUseCase          = parseCommandUseCase,
            appInfoLauncher              = appInfoLauncher,
            webSearchLauncher            = webSearchLauncher,
            setThemeUseCase              = setThemeUseCase,
            getSystemStatusUseCase       = getSystemStatusUseCase,
            getNotificationCountsUseCase = getNotificationCountsUseCase,
            getUiPrefsUseCase            = getUiPrefsUseCase,
            updateUiPrefsUseCase         = updateUiPrefsUseCase,
            clearWallpaperUseCase        = clearWallpaperUseCase
        )
    }

    fun appListViewModelFactory() = factory {
        AppListViewModel(getInstalledAppsUseCase, searchAppsUseCase, launchAppUseCase, trackAppLaunchUseCase)
    }

    fun settingsViewModelFactory() = factory {
        SettingsViewModel(
            getThemeUseCase, getUiPrefsUseCase, setThemeUseCase, updateUiPrefsUseCase,
            clearHistoryUseCase, observeWallpaperUseCase, setWallpaperUseCase, clearWallpaperUseCase
        )
    }

    fun securityViewModelFactory() = factory {
        SecurityViewModel(getSecurityReportUseCase, generatePasswordUseCase, hashTextUseCase)
    }

    fun insightsViewModelFactory() = factory {
        InsightsViewModel(getUsageStatsUseCase, getStepsUseCase)
    }

    fun statusBarViewModelFactory() = factory {
        StatusBarViewModel(getSystemStatusUseCase, getUiPrefsUseCase)
    }

    fun arsenalViewModelFactory() = factory {
        ArsenalViewModel(getArsenalModulesUseCase, observeArsenalModuleUseCase)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : ViewModel> factory(create: () -> T): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <V : ViewModel> create(modelClass: Class<V>): V = create() as V
        }
}
