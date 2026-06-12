package com.tien.tensor.domain.model

/** Size presets for the launcher's own status bar. */
enum class BarSize(val displayName: String) {
    COMPACT("S"),
    NORMAL("M"),
    LARGE("L")
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
 */
data class UiPrefs(
    val statusBarSize: BarSize = BarSize.NORMAL,
    val fontScale: Float = 1.0f,
    val use24hClock: Boolean = true,
    val showClockSeconds: Boolean = true,
    val marginTopDp: Int = 0,
    val marginBottomDp: Int = 0,
    val language: String = LANG_SYSTEM
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
    }
}
