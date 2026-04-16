package com.temm.activity_app.character

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import com.temm.R
import com.temm.data.model.custom.CharacterModel
import com.temm.databinding.ItemCharacterBinding

// ListAdapter manages the list internally — no need to create an ArrayList manually
// DiffCallback is the "comparator" — tells the adapter which items changed so only those get updated
class CharacterAdapter : ListAdapter<CharacterModel, CharacterAdapter.ViewHolder>(DiffCallback()) {

    // Fragment uses this lambda to listen for item clicks
    var onItemClick: ((CharacterModel) -> Unit) = {}

    // ViewHolder holds the binding for each item in the list
    inner class ViewHolder(val binding: ItemCharacterBinding) : RecyclerView.ViewHolder(binding.root)

    // Called when RecyclerView needs a new ViewHolder (inflates item_character.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCharacterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) // use getItem() instead of list[position] — ListAdapter owns the list
        val stream = holder.itemView.context.assets.open(item.avatarPath)
        holder.binding.ivCharacter.setImageDrawable(Drawable.createFromStream(stream, null))
        stream.close()

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

    // DiffCallback: ListAdapter uses these two functions to compare the old and new list
    class DiffCallback : DiffUtil.ItemCallback<CharacterModel>() {
        // Question 1: Is this the same item? → compare by ID
        override fun areItemsTheSame(oldItem: CharacterModel, newItem: CharacterModel) = oldItem.id == newItem.id
        // Question 2: Did the content change? → data class compares all fields with ==
        override fun areContentsTheSame(oldItem: CharacterModel, newItem: CharacterModel) = oldItem == newItem
    }
}
