package com.tien.tensor.data.repository

import com.tien.tensor.data.source.PreferencesDataSource
import com.tien.tensor.domain.model.ThemeId
import com.tien.tensor.domain.port.ThemeRepository
import kotlinx.coroutines.flow.Flow

class ThemeRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource
) : ThemeRepository {

    override fun getSelectedTheme(): Flow<ThemeId> = preferencesDataSource.themeId

    override suspend fun setTheme(themeId: ThemeId) {
        preferencesDataSource.setThemeId(themeId)
    }
}
