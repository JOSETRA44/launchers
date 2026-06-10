package com.tien.tensor.domain.model

sealed class CommandAction {
    data class WebSearch(val query: String) : CommandAction()
    data class OpenAppInfo(val appQuery: String) : CommandAction()
    data class LaunchApp(val appQuery: String) : CommandAction()
    data class PinApp(val appQuery: String) : CommandAction()
    data class UnpinApp(val appQuery: String) : CommandAction()
    data class SetTheme(val themeId: ThemeId) : CommandAction()
    data object ShowHelp : CommandAction()
    data object ClearHistory : CommandAction()
    data object OpenSettings : CommandAction()
    data object OpenAppList : CommandAction()
    data class Unknown(val input: String) : CommandAction()
}
