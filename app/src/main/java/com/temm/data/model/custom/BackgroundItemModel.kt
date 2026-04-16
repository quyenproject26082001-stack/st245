package com.temm.data.model.custom

data class BackgroundItemModel(
    val id: String,             // file name without extension, e.g. "1"
    val jsonPath: String,       // path to lottie json, e.g. "bg/1.json"
    val isUnlocked: Boolean = false,
    val isSelected: Boolean = false
)
