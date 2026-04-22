package com.cat.catpiano.music.dialog

import android.app.Activity
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.base.BaseDialog
import com.cat.catpiano.music.core.extensions.setBackgroundConnerSmooth
import com.cat.catpiano.music.databinding.DialogLoadingBinding

class WaitingDialog(val context: Activity) :
    BaseDialog<DialogLoadingBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    override fun initView() {}
    override fun initAction() {}
    override fun onDismissListener() {}
}