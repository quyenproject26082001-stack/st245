package com.temm.activity_app.language

import android.content.Context
import com.temm.R
import com.temm.core.base.BaseAdapter
import com.temm.core.extensions.gone
import com.temm.core.extensions.loadImageGlide
import com.temm.core.extensions.setOnSingleClick
import com.temm.core.extensions.visible
import com.temm.data.model.LanguageModel
import com.temm.databinding.ItemLanguageBinding

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