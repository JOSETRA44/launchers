package com.tien.tensor.data.arsenal

import com.tien.tensor.domain.port.ArsenalRegistry
import com.tien.tensor.domain.port.SecurityModule

/**
 * Plugin registry of the Security Arsenal. Adding a tool = implementing
 * [SecurityModule] and appending it to the constructor list in AppModule;
 * the hub UI discovers it automatically.
 */
class ArsenalRegistryImpl(private val modules: List<SecurityModule>) : ArsenalRegistry {
    override fun all(): List<SecurityModule> = modules
    override fun byId(id: String): SecurityModule? = modules.firstOrNull { it.meta.id == id }
}
