package com.temm.data.model.custom

data class CharacterModel(
    val id: String,              // ID duy nhất để DiffUtil nhận diện item
    val avatarPath: String,      // đường dẫn ảnh trong assets, vd: "skin/1/avatar.png"
    val isUnlocked: Boolean = false,
    val isSelected: Boolean = false
)
