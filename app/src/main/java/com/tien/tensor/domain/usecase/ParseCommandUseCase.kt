package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.CommandAction
import com.tien.tensor.domain.model.ThemeId

class ParseCommandUseCase {

    operator fun invoke(input: String): CommandAction? {
        val trimmed = input.trim()
        if (trimmed.isBlank() || !trimmed.startsWith("/")) return null
        val raw = trimmed.removePrefix("/").trim()
        if (raw.isBlank()) return CommandAction.ShowHelp

        val parts = raw.split("\\s+".toRegex(), limit = 2)
        val cmd   = parts[0].lowercase()
        val arg   = parts.getOrNull(1)?.trim() ?: ""

        return when (cmd) {
            "g", "search", "web"       -> arg.ifBlank { null }?.let { CommandAction.WebSearch(it) }
            "info"                     -> arg.ifBlank { null }?.let { CommandAction.OpenAppInfo(it) }
            "open", "launch", "run"    -> arg.ifBlank { null }?.let { CommandAction.LaunchApp(it) }
            "pin"                      -> arg.ifBlank { null }?.let { CommandAction.PinApp(it) }
            "unpin"                    -> arg.ifBlank { null }?.let { CommandAction.UnpinApp(it) }
            "theme"                    -> parseTheme(arg)
            "help", "?"               -> CommandAction.ShowHelp
            "clean", "clear", "cls"   -> CommandAction.ClearHistory
            "settings", "cfg"         -> CommandAction.OpenSettings
            "apps", "ls", "list"      -> CommandAction.OpenAppList
            else                       -> CommandAction.Unknown(raw)
        }
    }

    private fun parseTheme(arg: String): CommandAction = when (arg.trim().lowercase()) {
        "dark", "hacker", "green" -> CommandAction.SetTheme(ThemeId.HACKER_DARK)
        "cyan", "blue"            -> CommandAction.SetTheme(ThemeId.HACKER_CYAN)
        "matrix"                  -> CommandAction.SetTheme(ThemeId.MATRIX_GREEN)
        else                       -> CommandAction.Unknown("theme $arg")
    }
}
