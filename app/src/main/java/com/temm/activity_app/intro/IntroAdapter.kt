package com.temm.activity_app.intro

import android.content.Context
import com.temm.core.base.BaseAdapter
import com.temm.core.extensions.loadImageGlide
import com.temm.core.extensions.select
import com.temm.core.extensions.setTextContent
import com.temm.core.extensions.strings
import com.temm.data.model.IntroModel
import com.temm.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImageGlide(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}