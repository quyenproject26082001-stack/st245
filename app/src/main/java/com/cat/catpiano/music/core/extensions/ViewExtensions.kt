package com.cat.catpiano.music.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.cat.catpiano.music.R
import com.cat.catpiano.music.activity_app.main.MainActivity
import com.cat.catpiano.music.core.helper.RateHelper
import com.cat.catpiano.music.core.helper.SharePreferenceHelper
import com.cat.catpiano.music.core.utils.DataLocal
import com.cat.catpiano.music.core.utils.state.RateState

// ----------------------------
// Visibility extensions
// ----------------------------
fun View.visible() { visibility = View.VISIBLE }
fun View.invisible() { visibility = View.INVISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.select() { isSelected = true }

// ----------------------------
// Toast extensions
// ----------------------------
fun Activity.showToast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, ContextCompat.getString(this, message), duration).show()
}
fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// ----------------------------
// Navigation / Transition
// ----------------------------
fun Activity.handleBackLeftToRight() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}
fun Activity.handleBackRightToLeft() {
    finish()
    overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
}

fun Activity.goHome() {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
    finish() // optional: để chắc chắn đóng màn hiện tại
}


fun Activity.hideNavigation(isBlack: Boolean = true) {
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
    window.decorView.systemUiVisibility = if (isBlack) {
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    } else {
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}
// ----------------------------
// Click utils
// ----------------------------
fun View.setOnSingleClick(interval: Long = 200, action: (View) -> Unit) {
    setOnClickListener {
        if (System.currentTimeMillis() - DataLocal.lastClickTime >= interval) {
            action(it)
            DataLocal.lastClickTime = System.currentTimeMillis()
        }
    }
}
// ----------------------------
// TextView
// ----------------------------
fun TextView.setFont(@FontRes resId: Int) {
    typeface = ResourcesCompat.getFont(context, resId)
}

fun TextView.setTextContent(context: Context, resId: Int) {
    text = context.getString(resId)
}

fun Context.strings(resId: Int) : String {
    return getString(resId)
}

// Expand the touch area of a view by extraDp on each side without changing its visual size.
// Safe to call on multiple siblings sharing the same parent — chains delegates automatically.
fun View.expandTouchArea(extraDp: Int) {
    val extra = (extraDp * resources.displayMetrics.density).toInt()
    val parentView = parent as? View ?: return
    parentView.post {
        val rect = Rect()
        getHitRect(rect)
        rect.inset(-extra, -extra)
        val target = this
        val current = parentView.touchDelegate
        if (current == null) {
            parentView.touchDelegate = TouchDelegate(rect, target)
        } else {
            parentView.touchDelegate = object : TouchDelegate(rect, target) {
                override fun onTouchEvent(event: MotionEvent): Boolean =
                    current.onTouchEvent(event) || super.onTouchEvent(event)
            }
        }
    }
}
