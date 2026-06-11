package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.UiPrefs
import kotlinx.coroutines.flow.Flow

interface UiPrefsRepository {
    fun getPrefs(): Flow<UiPrefs>
    suspend fun update(prefs: UiPrefs)
}
