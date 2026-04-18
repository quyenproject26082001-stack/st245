package com.temm.data.model.custom

import android.graphics.drawable.Drawable

data class InstrumentModel(
    val id: String,
    val navPath: String,
    val noteCount: Int,
    val isSelected: Boolean = false,
    val isUnlocked: Boolean = false,
    val isAvailable: Boolean = true,
    val drawable: Drawable? = null
)
