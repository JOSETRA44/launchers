package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.ModuleMeta
import com.tien.tensor.domain.model.ModuleReport
import kotlinx.coroutines.flow.Flow

/**
 * Plugin contract of the Security Arsenal.
 *
 * Each tool implements this port in the data layer and is registered in the
 * [ArsenalRegistry]; the domain and presentation layers only ever see this
 * interface, so new tools are added without touching existing code.
 *
 * [observe] is a cold flow: one-shot modules emit a single report and
 * complete; streaming modules (telemetry, network watchers) emit until
 * cancelled. Re-collecting re-runs the scan.
 */
interface SecurityModule {
    val meta: ModuleMeta
    fun observe(): Flow<ModuleReport>
}

interface ArsenalRegistry {
    fun all(): List<SecurityModule>
    fun byId(id: String): SecurityModule?
}
