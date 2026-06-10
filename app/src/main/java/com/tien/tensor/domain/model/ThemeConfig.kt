package com.tien.tensor.domain.model

data class ThemeConfig(
    val id: ThemeId,
    val name: String = id.displayName
)
