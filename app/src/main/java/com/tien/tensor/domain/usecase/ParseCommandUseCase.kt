package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.BarSize
import com.tien.tensor.domain.model.CommandAction
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.model.UiPrefs

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
            "g", "search", "web"      -> arg.ifBlank { null }?.let { CommandAction.WebSearch(it) }
            "info"                    -> arg.ifBlank { null }?.let { CommandAction.OpenAppInfo(it) }
            "open", "launch", "run"   -> arg.ifBlank { null }?.let { CommandAction.LaunchApp(it) }
            "pin"                     -> arg.ifBlank { null }?.let { CommandAction.PinApp(it) }
            "unpin"                   -> arg.ifBlank { null }?.let { CommandAction.UnpinApp(it) }
            "theme"                   -> parseTheme(arg)
            // UI customization
            "bar"                     -> parseBarSize(arg)
            "font"                    -> parseFontScale(arg)
            "clock"                   -> parseClock(arg)
            "margin"                  -> parseMargin(arg)
            "lang", "language"        -> parseLanguage(arg)
            // Folder commands
            "mkdir"                   -> arg.ifBlank { null }?.let { CommandAction.CreateFolder(it) }
            "rmdir"                   -> arg.ifBlank { null }?.let { CommandAction.DeleteFolder(it) }
            "folder"                  -> arg.ifBlank { null }?.let { CommandAction.OpenFolder(it) }
            "group"                   -> parseGroup(arg)
            // Navigation & system
            "help", "?"              -> CommandAction.ShowHelp
            "clean", "clear", "cls"  -> CommandAction.ClearHistory
            "settings", "cfg"        -> CommandAction.OpenSettings
            "apps", "ls", "list"     -> CommandAction.OpenAppList
            "sec", "security", "audit" -> CommandAction.OpenSecurity
            "arsenal", "ars"         -> CommandAction.OpenArsenal
            "stats", "insights", "usage" -> CommandAction.OpenInsights
            else                      -> CommandAction.Unknown(raw)
        }
    }

    private fun parseTheme(arg: String): CommandAction = when (arg.trim().lowercase()) {
        "dark", "hacker", "green" -> CommandAction.SetTheme(ThemeId.HACKER_DARK)
        "cyan", "blue"            -> CommandAction.SetTheme(ThemeId.HACKER_CYAN)
        "matrix"                  -> CommandAction.SetTheme(ThemeId.MATRIX_GREEN)
        "amber", "orange"         -> CommandAction.SetTheme(ThemeId.AMBER_TERM)
        "red", "alert"            -> CommandAction.SetTheme(ThemeId.RED_ALERT)
        "ice", "arctic", "white"  -> CommandAction.SetTheme(ThemeId.ARCTIC_ICE)
        else                       -> CommandAction.Unknown("theme $arg")
    }

    private fun parseBarSize(arg: String): CommandAction = when (arg.trim().lowercase()) {
        "s", "small", "compact" -> CommandAction.SetBarSize(BarSize.COMPACT)
        "m", "medium", "normal" -> CommandAction.SetBarSize(BarSize.NORMAL)
        "l", "large", "big"     -> CommandAction.SetBarSize(BarSize.LARGE)
        else                     -> CommandAction.Unknown("bar $arg")
    }

    private fun parseFontScale(arg: String): CommandAction {
        val pct = arg.trim().removeSuffix("%").toIntOrNull() ?: return CommandAction.Unknown("font $arg")
        val scale = UiPrefs.FONT_SCALES.minByOrNull { kotlin.math.abs(it * 100 - pct) }
            ?: return CommandAction.Unknown("font $arg")
        return CommandAction.SetFontScale(scale)
    }

    private fun parseClock(arg: String): CommandAction = when (arg.trim().lowercase()) {
        "12", "12h" -> CommandAction.SetClockFormat(use24h = false)
        "24", "24h" -> CommandAction.SetClockFormat(use24h = true)
        else         -> CommandAction.Unknown("clock $arg")
    }

    /** `/margin <t|b> <dp>` — manual safety margin against physical screen edges. */
    private fun parseMargin(arg: String): CommandAction {
        val parts = arg.trim().split("\\s+".toRegex())
        if (parts.size != 2) return CommandAction.Unknown("margin $arg")
        val top = when (parts[0].lowercase()) {
            "t", "top"    -> true
            "b", "bottom" -> false
            else           -> return CommandAction.Unknown("margin $arg")
        }
        val dp = parts[1].toIntOrNull() ?: return CommandAction.Unknown("margin $arg")
        return CommandAction.SetMargin(top, dp.coerceIn(0, UiPrefs.MARGIN_MAX_DP))
    }

    private fun parseLanguage(arg: String): CommandAction {
        val tag = when (arg.trim().lowercase()) {
            "sys", "system", "auto" -> UiPrefs.LANG_SYSTEM
            in UiPrefs.LANGUAGES    -> arg.trim().lowercase()
            else                     -> return CommandAction.Unknown("lang $arg")
        }
        return CommandAction.SetLanguage(tag)
    }

    private fun parseGroup(arg: String): CommandAction? {
        val twoWords = arg.split("\\s+".toRegex(), limit = 2)
        if (twoWords.size < 2 || twoWords[1].isBlank()) return null
        return CommandAction.AddToFolder(twoWords[0], twoWords[1])
    }
}
