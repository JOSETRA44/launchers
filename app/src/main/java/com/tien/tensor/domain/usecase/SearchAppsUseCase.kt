package com.tien.tensor.domain.usecase

import com.tien.tensor.domain.model.AppInfo

class SearchAppsUseCase {
    operator fun invoke(query: String, apps: List<AppInfo>): List<AppInfo> {
        if (query.isBlank()) return apps
        val normalized = query.trim().lowercase()
        return apps.filter { it.appName.lowercase().contains(normalized) }
    }
}
