package com.cat.catpiano.music.core.custom.layout

import android.widget.ImageView
import com.cat.catpiano.music.core.custom.imageview.StrokeImageView

interface EventRatioFrame {
    fun onImageClick(image: StrokeImageView, btnEdit: ImageView)
}