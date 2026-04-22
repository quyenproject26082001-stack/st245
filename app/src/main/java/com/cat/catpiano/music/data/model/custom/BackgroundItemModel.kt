package com.cat.catpiano.music.data.model.custom

data class BackgroundItemModel(
    val id: String,
    val jsonPath: String,
    val isUnlocked: Boolean = false,
    val isSelected: Boolean = false,
    val previewResId: Int = 0
)
