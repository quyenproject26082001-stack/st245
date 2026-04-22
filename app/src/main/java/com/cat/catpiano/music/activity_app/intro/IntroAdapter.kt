package com.cat.catpiano.music.activity_app.intro

import android.content.Context
import com.cat.catpiano.music.core.base.BaseAdapter
import com.cat.catpiano.music.core.extensions.loadImageGlide
import com.cat.catpiano.music.core.extensions.select
import com.cat.catpiano.music.core.extensions.setTextContent
import com.cat.catpiano.music.core.extensions.strings
import com.cat.catpiano.music.data.model.IntroModel
import com.cat.catpiano.music.databinding.ItemIntroBinding

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