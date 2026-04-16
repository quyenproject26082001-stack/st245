package com.temm.dialog

import android.app.Activity
import com.temm.R
import com.temm.core.base.BaseDialog
import com.temm.core.extensions.setBackgroundConnerSmooth
import com.temm.databinding.DialogLoadingBinding

class WaitingDialog(val context: Activity) :
    BaseDialog<DialogLoadingBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    override fun initView() {
    }

    override fun initAction() {}

    override fun onDismissListener() {}

}