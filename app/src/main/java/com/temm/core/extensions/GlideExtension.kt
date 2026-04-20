package com.temm.core.extensions

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.temm.core.utils.DataLocal
import com.facebook.shimmer.ShimmerDrawable


fun loadImageGlide(context: Context, path: String, imageView: ImageView) {
    val shimmerDrawable = ShimmerDrawable().apply { setShimmer(DataLocal.shimmer) }
    Glide.with(context).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
}

fun loadImageGlide(viewGroup: ViewGroup, path: String, imageView: ImageView) {
    val shimmerDrawable = ShimmerDrawable().apply { setShimmer(DataLocal.shimmer) }
    Glide.with(viewGroup).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
}

fun loadImageGlide(viewGroup: ViewGroup, path: Int, imageView: ImageView, isLoadShimmer: Boolean = true) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (isLoadShimmer){
        Glide.with(viewGroup).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imageView)
    }else{
        Glide.with(viewGroup).load(path).into(imageView)
    }
}