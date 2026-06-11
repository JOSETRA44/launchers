package com.tien.tensor.presentation.arsenal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import com.tien.tensor.domain.usecase.GetArsenalModulesUseCase
import com.tien.tensor.domain.usecase.ObserveArsenalModuleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArsenalUiState(
    val modules: List<ModuleMeta> = emptyList(),
    val reports: Map<String, ModuleReport> = emptyMap(),
    /** One-shot modules currently scanning (no fresh report yet). */
    val scanningIds: Set<String> = emptySet(),
    val selectedModuleId: String? = null
) {
    val selectedModule: ModuleMeta?  get() = modules.firstOrNull { it.id == selectedModuleId }
    val selectedReport: ModuleReport? get() = selectedModuleId?.let { reports[it] }
}

/**
 * Drives the arsenal hub. Every registered plugin gets its own collection
 * job: one-shot modules run once on entry (re-run via [rescan]); streaming
 * modules keep emitting while the ViewModel lives. Jobs are independent —
 * a slow scan never blocks live telemetry.
 */
class ArsenalViewModel(
    private val getArsenalModulesUseCase: GetArsenalModulesUseCase,
    private val observeArsenalModuleUseCase: ObserveArsenalModuleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ArsenalUiState())
    val uiState: StateFlow<ArsenalUiState> = _state.asStateFlow()

    private val jobs = mutableMapOf<String, Job>()

    init {
        val modules = getArsenalModulesUseCase()
        _state.update { it.copy(modules = modules) }
        modules.forEach { startModule(it.id) }
    }

    fun rescan(moduleId: String) = startModule(moduleId)

    fun onSelectModule(moduleId: String) = _state.update { it.copy(selectedModuleId = moduleId) }
    fun onCloseDetail()                  = _state.update { it.copy(selectedModuleId = null) }

    private fun startModule(moduleId: String) {
        jobs[moduleId]?.cancel()
        _state.update { it.copy(scanningIds = it.scanningIds + moduleId) }
        jobs[moduleId] = viewModelScope.launch {
            observeArsenalModuleUseCase(moduleId)
                .onEach { report ->
                    _state.update {
                        it.copy(
                            reports     = it.reports + (moduleId to report),
                            scanningIds = it.scanningIds - moduleId
                        )
                    }
                }
                .collect {}
        }
    }
}
