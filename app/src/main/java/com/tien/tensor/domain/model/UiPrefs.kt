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
 */
data class UiPrefs(
    val statusBarSize: BarSize = BarSize.NORMAL,
    val fontScale: Float = 1.0f,
    val use24hClock: Boolean = true,
    val showClockSeconds: Boolean = true
) {
    companion object {
        val FONT_SCALES = listOf(0.9f, 1.0f, 1.1f, 1.25f)
    }
}
