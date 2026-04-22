package com.cat.catpiano.music.core.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.cat.catpiano.music.core.utils.DataLocal
import com.facebook.shimmer.ShimmerDrawable

// Try loading basePath+".webp", fallback to basePath+".png"
fun Context.openAssetImageDrawable(basePath: String): Drawable? {
    for (ext in listOf("webp", "png")) {
        try {
            return assets.open("$basePath.$ext").use { Drawable.createFromStream(it, null) }
        } catch (_: Exception) {}
    }
    return null
}

// Check if a folder in assets contains an image file (webp or png) with given base name
fun Context.assetFolderHasImage(folder: String, baseName: String): Boolean {
    val files = assets.list(folder) ?: return false
    return files.contains("$baseName.webp") || files.contains("$baseName.png")
}


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