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
 * [marginTopDp]/[marginBottomDp] are manual safety margins added on top of
 * system insets, for cases, curved glass or screen protectors that physically
 * cover screen edges. [language] is a BCP-47 tag or [LANG_SYSTEM].
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
        const val MARGIN_MAX_DP = 64
        const val MARGIN_STEP_DP = 4
        const val LANG_SYSTEM = "system"
        /** Languages the UI ships translations for; extend when adding a values-xx folder. */
        val LANGUAGES = listOf(LANG_SYSTEM, "en", "es")
    }
}
