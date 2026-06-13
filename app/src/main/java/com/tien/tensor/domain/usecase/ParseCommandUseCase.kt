package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.BarSize
import com.tien.tensor.domain.model.CommandAction
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.model.UiPrefs
import com.tien.tensor.domain.model.WallpaperAnchor

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
            "wall", "wallpaper"       -> parseWallpaper(arg)
            "date"                    -> parseDate(arg)
            "cursor"                  -> parseCursor(arg)
            "type", "typing"          -> parseTyping(arg)
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

    private fun parseBarSize(arg: String): CommandAction {
        val a = arg.trim().lowercase()
        if (a.startsWith("bg")) {
            return when (a.removePrefix("bg").trim()) {
                "on", "solid", "yes"  -> CommandAction.SetStatusBarOpaque(true)
                "off", "clear", "no"  -> CommandAction.SetStatusBarOpaque(false)
                else                   -> CommandAction.Unknown("bar $arg")
            }
        }
        return when (a) {
            "s", "small", "compact" -> CommandAction.SetBarSize(BarSize.COMPACT)
            "m", "medium", "normal" -> CommandAction.SetBarSize(BarSize.NORMAL)
            "l", "large", "big"     -> CommandAction.SetBarSize(BarSize.LARGE)
            else                     -> CommandAction.Unknown("bar $arg")
        }
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

    /**
     * `/margin <t|b> <±dp>` — signed margin against physical screen edges.
     * Negative values compensate the system inset/cutout padding so the UI
     * can sit flush against the hardware edge (effective padding never < 0).
     */
    private fun parseMargin(arg: String): CommandAction {
        val parts = arg.trim().split("\\s+".toRegex())
        if (parts.size != 2) return CommandAction.Unknown("margin $arg")
        val top = when (parts[0].lowercase()) {
            "t", "top"    -> true
            "b", "bottom" -> false
            else           -> return CommandAction.Unknown("margin $arg")
        }
        val dp = parts[1].toIntOrNull() ?: return CommandAction.Unknown("margin $arg")
        return CommandAction.SetMargin(top, dp.coerceIn(UiPrefs.MARGIN_MIN_DP, UiPrefs.MARGIN_MAX_DP))
    }

    /**
     * `/wall off | alpha <15|30|50|100> | size <40|60|80|100> | pos <tl|tr|c|bl|br|fill>`
     * Styles the wallpaper sticker layer. Picking the image itself needs the
     * system photo picker, so it lives in Settings, not here.
     */
    private fun parseWallpaper(arg: String): CommandAction {
        val parts = arg.trim().split("\\s+".toRegex())
        return when (parts[0].lowercase()) {
            "off", "clear", "none" -> CommandAction.ClearWallpaper
            "alpha", "opacity" -> {
                val pct = parts.getOrNull(1)?.removeSuffix("%")?.toIntOrNull()
                    ?: return CommandAction.Unknown("wall $arg")
                val alpha = UiPrefs.WALLPAPER_ALPHAS.minByOrNull { kotlin.math.abs(it * 100 - pct) }
                    ?: return CommandAction.Unknown("wall $arg")
                CommandAction.SetWallpaperAlpha(alpha)
            }
            "size" -> {
                val pct = parts.getOrNull(1)?.removeSuffix("%")?.toIntOrNull()
                    ?: return CommandAction.Unknown("wall $arg")
                val size = UiPrefs.WALLPAPER_SIZES.minByOrNull { kotlin.math.abs(it - pct) }
                    ?: return CommandAction.Unknown("wall $arg")
                CommandAction.SetWallpaperSize(size)
            }
            "pos", "position" -> when (parts.getOrNull(1)?.lowercase()) {
                "tl", "topleft"     -> CommandAction.SetWallpaperAnchor(WallpaperAnchor.TOP_LEFT)
                "tr", "topright"    -> CommandAction.SetWallpaperAnchor(WallpaperAnchor.TOP_RIGHT)
                "c", "center"       -> CommandAction.SetWallpaperAnchor(WallpaperAnchor.CENTER)
                "bl", "bottomleft"  -> CommandAction.SetWallpaperAnchor(WallpaperAnchor.BOTTOM_LEFT)
                "br", "bottomright" -> CommandAction.SetWallpaperAnchor(WallpaperAnchor.BOTTOM_RIGHT)
                "fill", "full"      -> CommandAction.SetWallpaperAnchor(WallpaperAnchor.FILL)
                else                 -> CommandAction.Unknown("wall $arg")
            }
            else -> CommandAction.Unknown("wall $arg")
        }
    }

    private fun parseLanguage(arg: String): CommandAction {
        val tag = when (arg.trim().lowercase()) {
            "sys", "system", "auto" -> UiPrefs.LANG_SYSTEM
            in UiPrefs.LANGUAGES    -> arg.trim().lowercase()
            else                     -> return CommandAction.Unknown("lang $arg")
        }
        return CommandAction.SetLanguage(tag)
    }

    private fun parseDate(arg: String): CommandAction = when (arg.trim().lowercase()) {
        "on", "show", "yes"   -> CommandAction.ToggleDate(true)
        "off", "hide", "no"   -> CommandAction.ToggleDate(false)
        else                   -> CommandAction.Unknown("date $arg")
    }

    private fun parseCursor(arg: String): CommandAction = when (arg.trim().lowercase()) {
        "blink", "animated"   -> CommandAction.SetCursorBlink(true)
        "static", "off", "still" -> CommandAction.SetCursorBlink(false)
        else                   -> CommandAction.Unknown("cursor $arg")
    }

    private fun parseTyping(arg: String): CommandAction {
        val ms = when (arg.trim().lowercase()) {
            "instant", "0"         -> 0
            "fast", "20"           -> 20
            "norm", "normal", "55" -> 55
            "slow", "150"          -> 150
            else                    -> return CommandAction.Unknown("type $arg")
        }
        return CommandAction.SetTypingSpeed(ms)
    }

    private fun parseGroup(arg: String): CommandAction? {
        val twoWords = arg.split("\\s+".toRegex(), limit = 2)
        if (twoWords.size < 2 || twoWords[1].isBlank()) return null
        return CommandAction.AddToFolder(twoWords[0], twoWords[1])
    }
}
