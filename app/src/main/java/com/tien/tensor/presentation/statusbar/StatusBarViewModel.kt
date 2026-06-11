package com.tien.tensor.presentation.statusbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.model.SystemStatus
import com.tien.tensor.domain.usecase.GetSystemStatusUseCase
import com.tien.tensor.domain.usecase.GetUiPrefsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class StatusBarUiState(
    val time: String = "",
    val status: SystemStatus = SystemStatus()
)

/**
 * Activity-scoped: the dynamic status bar lives above every destination,
 * replacing the hidden system bar while the launcher is in immersive mode.
 */
class StatusBarViewModel(
    private val getSystemStatusUseCase: GetSystemStatusUseCase,
    private val getUiPrefsUseCase: GetUiPrefsUseCase
) : ViewModel() {

    private var timeFmt = SimpleDateFormat("HH:mm", Locale.US)

    private val _state = MutableStateFlow(StatusBarUiState(time = now()))
    val uiState: StateFlow<StatusBarUiState> = _state.asStateFlow()

    private var statusJob: Job? = null

    init {
        startStatusCollection()
        viewModelScope.launch {
            getUiPrefsUseCase().collect { prefs ->
                timeFmt = SimpleDateFormat(if (prefs.use24hClock) "HH:mm" else "hh:mm a", Locale.US)
                _state.update { it.copy(time = now()) }
            }
        }
        viewModelScope.launch {
            while (true) {
                _state.update { it.copy(time = now()) }
                // Wake exactly on the next minute boundary — no per-second polling
                delay(60_000 - System.currentTimeMillis() % 60_000)
            }
        }
    }

    /** Re-subscribes the hardware flow, e.g. after READ_PHONE_STATE is granted. */
    fun restartStatusCollection() = startStatusCollection()

    private fun startStatusCollection() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            getSystemStatusUseCase().collect { status ->
                _state.update { it.copy(status = status) }
            }
        }
    }

    private fun now(): String = timeFmt.format(Calendar.getInstance().time)
}
