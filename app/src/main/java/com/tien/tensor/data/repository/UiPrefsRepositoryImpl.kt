package com.tien.tensor.data.repository

import com.tien.tensor.data.source.UiPrefsDataSource
import com.tien.tensor.domain.model.UiPrefs
import com.tien.tensor.domain.port.UiPrefsRepository
import kotlinx.coroutines.flow.Flow

class UiPrefsRepositoryImpl(private val dataSource: UiPrefsDataSource) : UiPrefsRepository {
    override fun getPrefs(): Flow<UiPrefs> = dataSource.prefs
    override suspend fun update(prefs: UiPrefs) = dataSource.update(prefs)
}
