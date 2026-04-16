package com.temm.activity_app.background

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.temm.R
import com.temm.data.model.custom.BackgroundItemModel
import com.temm.databinding.ItemBackgroundBinding

class BackgroundAdapter : ListAdapter<BackgroundItemModel, BackgroundAdapter.ViewHolder>(DiffCallback()) {

    // Fragment uses this lambda to listen for item clicks
    var onItemClick: ((BackgroundItemModel) -> Unit) = {}

    // ViewHolder holds the binding for each item in the list
    inner class ViewHolder(val binding: ItemBackgroundBinding) : RecyclerView.ViewHolder(binding.root)

    // Called when RecyclerView needs a new ViewHolder (inflates item_background.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBackgroundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) // use getItem() — ListAdapter owns the list
        // Lottie 6.x throws by default on parse failure — override to prevent crash
        holder.binding.lottiePreview.setFailureListener { /* ignore parse errors */ }
        holder.binding.lottiePreview.setAnimation(item.jsonPath)
        holder.binding.lottiePreview.playAnimation() // lottie_autoPlay in XML only triggers at inflation, not after setAnimation()

        // Three states: unlock (locked) → use (unlocked, not active) → used (currently active)
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

    // DiffCallback: compares old and new list to find the minimum changes
    class DiffCallback : DiffUtil.ItemCallback<BackgroundItemModel>() {
        // Question 1: Is this the same item? → compare by ID
        override fun areItemsTheSame(oldItem: BackgroundItemModel, newItem: BackgroundItemModel) = oldItem.id == newItem.id
        // Question 2: Did the content change? → data class compares all fields with ==
        override fun areContentsTheSame(oldItem: BackgroundItemModel, newItem: BackgroundItemModel) = oldItem == newItem
    }
}
