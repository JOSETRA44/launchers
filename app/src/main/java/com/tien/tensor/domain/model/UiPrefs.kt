package com.tien.tensor.domain.model

/** Size presets for the launcher's own status bar. */
enum class BarSize(val displayName: String) {
    COMPACT("S"),
    NORMAL("M"),
    LARGE("L")
}

/**
 * Where the wallpaper sticker is anchored on screen. [FILL] switches to a
 * classic full-bleed wallpaper (crop-to-fill, ignores the size preference).
 */
enum class WallpaperAnchor(val displayName: String) {
    TOP_LEFT("TL"),
    TOP_RIGHT("TR"),
    CENTER("C"),
    BOTTOM_LEFT("BL"),
    BOTTOM_RIGHT("BR"),
    FILL("FILL")
}

/**
 * User-tunable UI preferences, persisted in DataStore.
 * [fontScale] multiplies the whole terminal typography ramp.
 * [marginTopDp]/[marginBottomDp] are SIGNED manual margins relative to the
 * system insets: positive values push the UI away from physical edges (cases,
 * curved glass, protectors); negative values compensate the inset/cutout
 * padding so the UI can sit flush against the hardware edge. The effective
 * padding is `max(0, inset + margin)`, computed in MainActivity.
 * [language] is a BCP-47 tag or [LANG_SYSTEM].
 * [wallpaperAlpha]/[wallpaperSizePct]/[wallpaperAnchor] style the optional
 * wallpaper sticker layer (the image itself lives behind WallpaperRepository);
 * the default 30% alpha keeps terminal text readable on top of any image.
 */
data class UiPrefs(
    val statusBarSize: BarSize = BarSize.NORMAL,
    val fontScale: Float = 1.0f,
    val use24hClock: Boolean = true,
    val showClockSeconds: Boolean = true,
    val marginTopDp: Int = 0,
    val marginBottomDp: Int = 0,
    val language: String = LANG_SYSTEM,
    val wallpaperAlpha: Float = 0.30f,
    val wallpaperSizePct: Int = 60,
    val wallpaperAnchor: WallpaperAnchor = WallpaperAnchor.BOTTOM_RIGHT
) {
    companion object {
        val FONT_SCALES = listOf(0.9f, 1.0f, 1.1f, 1.25f)
        /** Most negative margin: enough to cancel any status-bar/cutout inset. */
        const val MARGIN_MIN_DP = -64
        const val MARGIN_MAX_DP = 64
        const val MARGIN_STEP_DP = 4
        const val LANG_SYSTEM = "system"
        /** Languages the UI ships translations for; extend when adding a values-xx folder. */
        val LANGUAGES = listOf(LANG_SYSTEM, "en", "es")
        /** Wallpaper opacity presets (fraction of full opacity). */
        val WALLPAPER_ALPHAS = listOf(0.15f, 0.30f, 0.50f, 1.0f)
        /** Wallpaper sticker width presets (% of screen width). */
        val WALLPAPER_SIZES = listOf(40, 60, 80, 100)
    }
}
