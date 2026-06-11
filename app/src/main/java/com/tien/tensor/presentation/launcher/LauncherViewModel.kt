package com.tien.tensor.presentation.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.model.AppInfo
import com.tien.tensor.domain.model.CommandAction
import com.tien.tensor.domain.model.SmartApp
import com.tien.tensor.domain.port.AppInfoLauncher
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
import com.tien.tensor.domain.usecase.LaunchAppUseCase
import com.tien.tensor.domain.usecase.ParseCommandUseCase
import com.tien.tensor.domain.usecase.PinAppUseCase
import com.tien.tensor.domain.usecase.RemoveFromFolderUseCase
import com.tien.tensor.domain.usecase.SearchAppsUseCase
import com.tien.tensor.domain.usecase.SetThemeUseCase
import com.tien.tensor.domain.usecase.TrackAppLaunchUseCase
import com.tien.tensor.domain.usecase.UnpinAppUseCase
import com.tien.tensor.presentation.navigation.AppDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LauncherViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val getSmartAppsUseCase: GetSmartAppsUseCase,
    private val getPinnedAppsUseCase: GetPinnedAppsUseCase,
    private val getFoldersUseCase: GetFoldersUseCase,
    private val searchAppsUseCase: SearchAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val trackAppLaunchUseCase: TrackAppLaunchUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val pinAppUseCase: PinAppUseCase,
    private val unpinAppUseCase: UnpinAppUseCase,
    private val createFolderUseCase: CreateFolderUseCase,
    private val addToFolderUseCase: AddToFolderUseCase,
    private val removeFromFolderUseCase: RemoveFromFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val parseCommandUseCase: ParseCommandUseCase,
    private val appInfoLauncher: AppInfoLauncher,
    private val webSearchLauncher: WebSearchLauncher,
    private val setThemeUseCase: SetThemeUseCase,
    private val getSystemStatusUseCase: GetSystemStatusUseCase,
    private val getNotificationCountsUseCase: GetNotificationCountsUseCase
) : ViewModel() {

    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val dateFmt = SimpleDateFormat("yyyy.MM.dd | EEE", Locale.US)

    private val _state = MutableStateFlow(
        LauncherUiState(currentTime = formattedTime(), currentDate = formattedDate())
    )
    val uiState: StateFlow<LauncherUiState> = _state.asStateFlow()

    private val _navEvents = MutableSharedFlow<AppDestination>(extraBufferCapacity = 1)
    val navigationEvents: SharedFlow<AppDestination> = _navEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            getInstalledAppsUseCase().collect { apps ->
                _state.update { s -> s.copy(isLoading = false, allApps = apps, searchResults = smartSearch(s.searchQuery, apps, s.smartApps)) }
            }
        }
        viewModelScope.launch {
            getSmartAppsUseCase().collect { smart ->
                _state.update { s -> s.copy(smartApps = smart, searchResults = smartSearch(s.searchQuery, s.allApps, smart)) }
            }
        }
        viewModelScope.launch { getPinnedAppsUseCase().collect { p -> _state.update { it.copy(pinnedApps = p) } } }
        viewModelScope.launch { getFoldersUseCase().collect  { f -> _state.update { it.copy(folders = f) } } }
        viewModelScope.launch { getSystemStatusUseCase().collect { s -> _state.update { it.copy(systemStatus = s) } } }
        viewModelScope.launch { getNotificationCountsUseCase().collect { c -> _state.update { it.copy(notificationCounts = c) } } }
        viewModelScope.launch {
            while (true) { delay(1_000); _state.update { it.copy(currentTime = formattedTime(), currentDate = formattedDate()) } }
        }
    }

    // ── Search & commands ─────────────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _state.update { s -> s.copy(searchQuery = query, searchResults = smartSearch(query, s.allApps, s.smartApps)) }
    }

    fun onSearchSubmit() {
        val s     = _state.value
        val input = s.searchQuery.trim()
        if (input.isBlank()) return
        val action = parseCommandUseCase(input)
        if (action != null) { addToHistory(input); executeCommand(action) }
        else { val top = s.searchResults.firstOrNull() ?: s.allApps.firstOrNull() ?: return; onAppLaunch(top.packageName, top.appName) }
        _state.update { it.copy(searchQuery = "") }
    }

    // ── App actions ───────────────────────────────────────────────────────────

    fun onAppLaunch(packageName: String, appName: String) {
        viewModelScope.launch {
            _state.update { it.copy(launchingAppName = appName) }
            launchAppUseCase(packageName)
            trackAppLaunchUseCase(packageName)
            delay(700)
            _state.update { it.copy(launchingAppName = null) }
        }
    }

    // ── Overlay actions ───────────────────────────────────────────────────────

    fun onDismissHelp()                { _state.update { it.copy(showHelp = false) } }
    fun onHistoryTap(cmd: String)      { _state.update { it.copy(searchQuery = cmd) } }
    fun onOpenFolder(folderId: String) { _state.update { it.copy(activeFolderId = folderId) } }
    fun onCloseFolderOverlay()         { _state.update { it.copy(activeFolderId = null) } }

    fun onRemoveFromFolder(folderId: String, packageName: String) {
        viewModelScope.launch { removeFromFolderUseCase(folderId, packageName) }
    }

    // ── Command execution ─────────────────────────────────────────────────────

    private fun executeCommand(action: CommandAction) {
        when (action) {
            is CommandAction.WebSearch  -> { webSearchLauncher.search(action.query); showOutput("> Searching: \"${action.query}\"...") }
            is CommandAction.OpenAppInfo -> {
                val app = fuzzyFind(action.appQuery) ?: run { showOutput("> Not found: \"${action.appQuery}\""); return }
                appInfoLauncher.open(app.packageName); showOutput("> Info: ${app.appName}")
            }
            is CommandAction.LaunchApp  -> {
                val app = fuzzyFind(action.appQuery) ?: run { showOutput("> Not found: \"${action.appQuery}\""); return }
                onAppLaunch(app.packageName, app.appName)
            }
            is CommandAction.PinApp     -> {
                val app = fuzzyFind(action.appQuery) ?: run { showOutput("> Not found: \"${action.appQuery}\""); return }
                viewModelScope.launch {
                    val ok = pinAppUseCase(app.packageName, app.appName)
                    showOutput(if (ok) "> Pinned: ${app.appName}" else "> Dock full or already pinned.")
                }
            }
            is CommandAction.UnpinApp   -> {
                val app = fuzzyFind(action.appQuery) ?: run { showOutput("> Not found: \"${action.appQuery}\""); return }
                viewModelScope.launch { unpinAppUseCase(app.packageName); showOutput("> Unpinned: ${app.appName}") }
            }
            is CommandAction.SetTheme   -> { viewModelScope.launch { setThemeUseCase(action.themeId); showOutput("> Theme: ${action.themeId.displayName}") } }
            // Folder commands
            is CommandAction.CreateFolder -> {
                if (_state.value.folders.any { it.name.equals(action.name, ignoreCase = true) }) { showOutput("> \"${action.name}\" already exists."); return }
                viewModelScope.launch { createFolderUseCase(action.name); showOutput("> Folder \"${action.name}\" created.") }
            }
            is CommandAction.AddToFolder -> {
                val folder = _state.value.folders.firstOrNull { it.name.equals(action.folderName, ignoreCase = true) }
                    ?: run { showOutput("> Folder \"${action.folderName}\" not found."); return }
                val app = fuzzyFind(action.appQuery) ?: run { showOutput("> Not found: \"${action.appQuery}\""); return }
                viewModelScope.launch {
                    val ok = addToFolderUseCase(folder.id, app.packageName)
                    showOutput(if (ok) "> Added ${app.appName} → ${folder.name}" else "> Already in folder.")
                }
            }
            is CommandAction.DeleteFolder -> {
                val folder = _state.value.folders.firstOrNull { it.name.equals(action.folderName, ignoreCase = true) }
                    ?: run { showOutput("> Folder \"${action.folderName}\" not found."); return }
                viewModelScope.launch { deleteFolderUseCase(folder.id); showOutput("> Folder \"${folder.name}\" deleted.") }
            }
            is CommandAction.OpenFolder -> {
                val folder = _state.value.folders.firstOrNull { it.name.equals(action.folderName, ignoreCase = true) }
                    ?: run { showOutput("> Folder \"${action.folderName}\" not found."); return }
                _state.update { it.copy(activeFolderId = folder.id) }
            }
            CommandAction.ShowHelp      -> _state.update { it.copy(showHelp = true, commandOutput = null) }
            CommandAction.ClearHistory  -> { viewModelScope.launch { clearHistoryUseCase(); showOutput("> Launch history cleared.") } }
            CommandAction.OpenSettings  -> { viewModelScope.launch { _navEvents.emit(AppDestination.SETTINGS) }; showOutput("> Opening settings...") }
            CommandAction.OpenAppList   -> { viewModelScope.launch { _navEvents.emit(AppDestination.APP_LIST) }; showOutput("> Opening apps...") }
            is CommandAction.Unknown    -> showOutput("> Unknown: \"${action.input}\". Type /help.")
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun showOutput(msg: String) {
        _state.update { it.copy(commandOutput = msg) }
        viewModelScope.launch {
            delay(3_500)
            _state.update { s -> if (s.commandOutput == msg) s.copy(commandOutput = null) else s }
        }
    }

    private fun addToHistory(input: String) {
        _state.update { s -> s.copy(commandHistory = (listOf(input) + s.commandHistory.filter { it != input }).take(10)) }
    }

    private fun fuzzyFind(query: String): AppInfo? {
        val apps = _state.value.allApps; val q = query.trim().lowercase()
        return apps.firstOrNull { it.appName.lowercase() == q }
            ?: apps.firstOrNull { it.appName.lowercase().startsWith(q) }
            ?: apps.firstOrNull { it.appName.lowercase().contains(q) }
    }

    private fun smartSearch(query: String, apps: List<AppInfo>, smart: List<SmartApp>): List<AppInfo> {
        val base = searchAppsUseCase(query, apps)
        if (query.isBlank()) return base
        val q = query.trim().lowercase(); val recentPkgs = smart.map { it.packageName }.toHashSet()
        return base.sortedWith(
            compareByDescending<AppInfo> { it.appName.lowercase().startsWith(q) }
                .thenByDescending { it.packageName in recentPkgs }.thenBy { it.appName.lowercase() }
        )
    }

    private fun formattedTime() = timeFmt.format(Calendar.getInstance().time)
    private fun formattedDate() = dateFmt.format(Calendar.getInstance().time).uppercase()
}
