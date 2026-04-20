package com.temm.core.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import androidx.core.graphics.toColorInt
import com.temm.core.custom.background.SmoothCornerTreatment
import com.temm.core.helper.UnitHelper


// Conner
fun setBackgroundConnerSmooth(context: Context, view: View, radius: Int = 8){
    val drawableShape = MaterialShapeDrawable().apply {
        fillColor = ColorStateList.valueOf("#FFE9A9".toColorInt())
        setStroke(UnitHelper.pxToDpFloat(context, 2), "#F68300".toColorInt())
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCorners(SmoothCornerTreatment(1f))
            .setAllCornerSizes(UnitHelper.pxToDpFloat(context, radius))
            .build()
    }
    view.background = drawableShape
}

fun setBackgroundConnerSmooth(context: Context, view: View, solidColor: String, radius: Int = 8){
    val drawableShape = MaterialShapeDrawable().apply {
        fillColor = ColorStateList.valueOf(solidColor.toColorInt())
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCorners(SmoothCornerTreatment(1f))
            .setAllCornerSizes(UnitHelper.pxToDpFloat(context, radius))
            .build()
    }
    view.background = drawableShape
}
fun setBackgroundConnerSmooth(context: Context, view: View, solidColor: String, strokeColor: String, radius: Int = 8){
    val drawableShape = MaterialShapeDrawable().apply {
        fillColor = ColorStateList.valueOf(solidColor.toColorInt())
        setStroke(UnitHelper.pxToDpFloat(context, 2), strokeColor.toColorInt())
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCorners(SmoothCornerTreatment(1f))
            .setAllCornerSizes(UnitHelper.pxToDpFloat(context, radius))
            .build()
    }
    view.background = drawableShape
}
fun setBackgroundConnerSmooth(context: Context, view: View, solidColor: String, strokeColor: String, strokeWidth: Int, radius: Int = 8){
    val drawableShape = MaterialShapeDrawable().apply {
        fillColor = ColorStateList.valueOf(solidColor.toColorInt())
        setStroke(UnitHelper.pxToDpFloat(context, strokeWidth), strokeColor.toColorInt())
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCorners(SmoothCornerTreatment(1f))
            .setAllCornerSizes(UnitHelper.pxToDpFloat(context, radius))
            .build()
    }
    view.background = drawableShape
}
