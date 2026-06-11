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
import com.tien.tensor.data.repository.SystemStatusRepositoryImpl
import com.tien.tensor.data.repository.ThemeRepositoryImpl
import com.tien.tensor.data.source.AppDataSource
import com.tien.tensor.data.source.AppUsageDataSource
import com.tien.tensor.data.source.FolderDataSource
import com.tien.tensor.data.source.PinnedAppsDataSource
import com.tien.tensor.data.source.PreferencesDataSource
import com.tien.tensor.data.source.SystemStatusDataSource
import com.tien.tensor.domain.port.AppInfoLauncher
import com.tien.tensor.domain.port.AppLauncher
import com.tien.tensor.domain.port.AppRepository
import com.tien.tensor.domain.port.AppUsageRepository
import com.tien.tensor.domain.port.FolderRepository
import com.tien.tensor.domain.port.NotificationRepository
import com.tien.tensor.domain.port.PinnedAppsRepository
import com.tien.tensor.domain.port.SystemStatusRepository
import com.tien.tensor.domain.port.ThemeRepository
import com.tien.tensor.domain.port.WebSearchLauncher
import com.tien.tensor.domain.usecase.AddToFolderUseCase
import com.tien.tensor.domain.usecase.ClearHistoryUseCase
import com.tien.tensor.domain.usecase.CreateFolderUseCase
import com.tien.tensor.domain.usecase.DeleteFolderUseCase
import com.tien.tensor.domain.usecase.GetFoldersUseCase
import com.tien.tensor.domain.usecase.GetInstalledAppsUseCase
import com.tien.tensor.domain.usecase.GetNotificationCountsUseCase
import com.tien.tensor.domain.usecase.GetPinnedAppsUseCase
import com.tien.tensor.domain.usecase.GetSmartAppsUseCase
import com.tien.tensor.domain.usecase.GetSystemStatusUseCase
import com.tien.tensor.domain.usecase.GetThemeUseCase
import com.tien.tensor.domain.usecase.LaunchAppUseCase
import com.tien.tensor.domain.usecase.ParseCommandUseCase
import com.tien.tensor.domain.usecase.PinAppUseCase
import com.tien.tensor.domain.usecase.RemoveFromFolderUseCase
import com.tien.tensor.domain.usecase.SearchAppsUseCase
import com.tien.tensor.domain.usecase.SetThemeUseCase
import com.tien.tensor.domain.usecase.TrackAppLaunchUseCase
import com.tien.tensor.domain.usecase.UnpinAppUseCase
import com.tien.tensor.presentation.applist.AppListViewModel
import com.tien.tensor.presentation.launcher.LauncherViewModel
import com.tien.tensor.presentation.settings.SettingsViewModel
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

    private val appDataSource         by lazy { AppDataSource(appContext.packageManager) }
    private val preferencesDataSource by lazy { PreferencesDataSource(themeDataStore) }
    private val appUsageDataSource    by lazy { AppUsageDataSource(usageDataStore) }
    private val pinnedAppsDataSource  by lazy { PinnedAppsDataSource(pinnedDataStore) }
    private val folderDataSource      by lazy { FolderDataSource(folderDataStore) }
    private val systemStatusDataSource by lazy { SystemStatusDataSource(appContext) }

    // ── Repositories ──────────────────────────────────────────────────────────

    private val appRepository: AppRepository           by lazy { AppRepositoryImpl(appDataSource, appContext) }
    private val themeRepository: ThemeRepository       by lazy { ThemeRepositoryImpl(preferencesDataSource) }
    private val appUsageRepository: AppUsageRepository by lazy { AppUsageRepositoryImpl(appUsageDataSource) }
    private val pinnedAppsRepository: PinnedAppsRepository by lazy { PinnedAppsRepositoryImpl(pinnedAppsDataSource) }
    private val folderRepository: FolderRepository     by lazy { FolderRepositoryImpl(folderDataSource) }
    private val systemStatusRepository: SystemStatusRepository by lazy { SystemStatusRepositoryImpl(systemStatusDataSource) }
    private val notificationRepository: NotificationRepository by lazy { NotificationRepositoryImpl() }

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
            getNotificationCountsUseCase = getNotificationCountsUseCase
        )
    }

    fun appListViewModelFactory() = factory {
        AppListViewModel(getInstalledAppsUseCase, searchAppsUseCase, launchAppUseCase, trackAppLaunchUseCase)
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
