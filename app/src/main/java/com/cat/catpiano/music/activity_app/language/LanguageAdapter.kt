package com.cat.catpiano.music.activity_app.language

import android.content.Context
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.base.BaseAdapter
import com.cat.catpiano.music.core.extensions.gone
import com.cat.catpiano.music.core.extensions.loadImageGlide
import com.cat.catpiano.music.core.extensions.setOnSingleClick
import com.cat.catpiano.music.core.extensions.visible
import com.cat.catpiano.music.data.model.LanguageModel
import com.cat.catpiano.music.databinding.ItemLanguageBinding

class LanguageAdapter(val context: Context) : BaseAdapter<LanguageModel, ItemLanguageBinding>(
    ItemLanguageBinding::inflate
) {
    var onItemClick: ((String) -> Unit) = {}
    override fun onBind(
        binding: ItemLanguageBinding, item: LanguageModel, position: Int
    ) {
        binding.apply {
            loadImageGlide(root, item.flag, imvFlag, false)
            tvLang.text = item.name

            if (item.activate) {
                langSelect.setBackgroundResource(R.drawable.bg_language_focus)

            } else {
                langSelect.setBackgroundResource(R.drawable.bg_language)
            }

            root.setOnSingleClick {
                onItemClick.invoke(item.code)
            }
        }
    }

}