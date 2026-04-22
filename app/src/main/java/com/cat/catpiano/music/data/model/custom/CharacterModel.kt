package com.cat.catpiano.music.data.model.custom

import android.graphics.drawable.Drawable

data class CharacterModel(
    val id: String,
    val avatarPath: String,
    val isUnlocked: Boolean = false,
    val isSelected: Boolean = false,
    val drawable: Drawable? = null
)
