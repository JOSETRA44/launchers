package com.tien.tensor.domain.model

sealed class CommandAction {
    data class WebSearch(val query: String) : CommandAction()
    data class OpenAppInfo(val appQuery: String) : CommandAction()
    data class LaunchApp(val appQuery: String) : CommandAction()
    data class PinApp(val appQuery: String) : CommandAction()
    data class UnpinApp(val appQuery: String) : CommandAction()
    data class SetTheme(val themeId: ThemeId) : CommandAction()
    // UI customization
    data class SetBarSize(val size: BarSize) : CommandAction()
    data class SetFontScale(val scale: Float) : CommandAction()
    data class SetClockFormat(val use24h: Boolean) : CommandAction()
    // Folders
    data class CreateFolder(val name: String) : CommandAction()
    data class AddToFolder(val folderName: String, val appQuery: String) : CommandAction()
    data class DeleteFolder(val folderName: String) : CommandAction()
    data class OpenFolder(val folderName: String) : CommandAction()
    // System
    data object ShowHelp : CommandAction()
    data object ClearHistory : CommandAction()
    data object OpenSettings : CommandAction()
    data object OpenAppList : CommandAction()
    data object OpenSecurity : CommandAction()
    data object OpenInsights : CommandAction()
    data object OpenArsenal : CommandAction()
    data class Unknown(val input: String) : CommandAction()
}
