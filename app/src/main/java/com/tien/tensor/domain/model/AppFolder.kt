package com.tien.tensor.domain.model

data class AppFolder(
    val id: String,
    val name: String,
    val packageNames: List<String> = emptyList()
)
