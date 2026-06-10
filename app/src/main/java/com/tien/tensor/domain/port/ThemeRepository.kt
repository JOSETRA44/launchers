package com.tien.tensor.domain.port

import com.tien.tensor.domain.model.ThemeId
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun getSelectedTheme(): Flow<ThemeId>
    suspend fun setTheme(themeId: ThemeId)
}
