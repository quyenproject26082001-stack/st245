package com.temm.core.custom.layout

import android.widget.ImageView
import com.temm.core.custom.imageview.StrokeImageView

interface EventRatioFrame {
    fun onImageClick(image: StrokeImageView, btnEdit: ImageView)
}