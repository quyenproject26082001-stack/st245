package com.cat.catpiano.music.activity_app.background

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cat.catpiano.music.R
import com.cat.catpiano.music.data.model.custom.BackgroundItemModel
import com.cat.catpiano.music.databinding.ItemBackgroundBinding

class BackgroundAdapter : RecyclerView.Adapter<BackgroundAdapter.ViewHolder>() {

    var onItemClick: ((BackgroundItemModel) -> Unit) = {}

    private val items = mutableListOf<BackgroundItemModel>()
    val currentList: List<BackgroundItemModel> get() = items

    inner class ViewHolder(val binding: ItemBackgroundBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBackgroundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        if (item.previewResId != 0) holder.binding.ivPreview.setImageResource(item.previewResId)
        when {
            !item.isUnlocked -> {
                holder.binding.tvUse.setText(R.string.unlock)
                holder.binding.tvUse.setBackgroundResource(R.drawable.bg_unlock_character)
            }
            item.isSelected -> {
                holder.binding.tvUse.setText(R.string.used)
                holder.binding.tvUse.setBackgroundResource(R.drawable.bg_used_character)
            }
            else -> {
                holder.binding.tvUse.setText(R.string.use)
                holder.binding.tvUse.setBackgroundResource(R.drawable.bg_use_character)
            }
        }
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    fun submitList(newList: List<BackgroundItemModel>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(op: Int, np: Int) = items[op].id == newList[np].id
            override fun areContentsTheSame(op: Int, np: Int) =
                items[op].isUnlocked == newList[np].isUnlocked &&
                items[op].isSelected == newList[np].isSelected
        })
        items.clear()
        items.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }
}
