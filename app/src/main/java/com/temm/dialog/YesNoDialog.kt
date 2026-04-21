package com.temm.dialog

import android.app.Activity
import com.temm.core.extensions.gone
import com.temm.core.extensions.hideNavigation
import com.temm.core.extensions.setOnSingleClick
import com.temm.R
import com.temm.core.base.BaseDialog
import com.temm.core.extensions.strings
import com.temm.databinding.DialogConfirmBinding


class YesNoDialog(
    val context: Activity, val description: Int, val isError: Boolean = false
) : BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: (() -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    override fun initView() {
        initBottom()
        initText()
        if (isError) {
            binding.flBottom.btnBottomLeft.gone()
        }
        context.hideNavigation()
    }

    override fun initAction() {
        binding.apply {
            flBottom.btnBottomLeft.setOnSingleClick {
                onNoClick.invoke()
            }
            flBottom.btnBottomRight.setOnSingleClick {
                onYesClick.invoke()
            }
            flOutSide.setOnSingleClick {
                onDismissClick.invoke()
            }
        }
    }

    override fun onDismissListener() {

    }

    private fun initText() {
        binding.apply {
           // tvTitle.text = localizedContext.getString(title)
            tvDescription.text = localizedContext.getString(description)
        }
    }

    private fun initBottom() {
        binding.flBottom.apply {


            tvBottomLeft.text = localizedContext.strings(R.string.no)
            tvBottomRight.text = localizedContext.strings(R.string.yes)
        }
    }
}