package com.cat.catpiano.music.core.extensions

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.utils.key.IntentKey

fun Activity.startIntentRightToLeft(targetActivity: Class<*>) {
    val intent = Intent(this, targetActivity)
    val option =
        ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
    startActivity(intent, option.toBundle())
}

inline fun <reified T> Activity.startIntentRightToLeft(targetActivity: Class<*>, value: T) {
    val intent = Intent(this, targetActivity)
    when (value) {
        is String -> intent.putExtra(IntentKey.INTENT_KEY, value)
        is Int -> intent.putExtra(IntentKey.INTENT_KEY, value)
        is Boolean -> intent.putExtra(IntentKey.INTENT_KEY, value)
    }
    val option = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
    startActivity(intent, option.toBundle())
}

fun Activity.startIntentWithClearTop(targetActivity: Class <*>) {
    val intent = Intent(this, targetActivity)
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
}
