package com.temm.activity_app.instrument

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import com.temm.R
import com.temm.data.model.custom.InstrumentModel
import com.temm.databinding.ItemInstrumentBinding

class InstrumentAdapter : ListAdapter<InstrumentModel, InstrumentAdapter.ViewHolder>(DiffCallback()) {

    // Fragment uses this lambda to listen for item clicks
    var onItemClick: ((InstrumentModel) -> Unit) = {}

    // ViewHolder holds the binding for each item in the list
    inner class ViewHolder(val binding: ItemInstrumentBinding) : RecyclerView.ViewHolder(binding.root)

    // Called when RecyclerView needs a new ViewHolder (inflates item_instrument.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInstrumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) // use getItem() — ListAdapter owns the list
        val stream = holder.itemView.context.assets.open(item.navPath)
        holder.binding.ivCharacter.setImageDrawable(Drawable.createFromStream(stream, null))
        stream.close()
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
    class DiffCallback : DiffUtil.ItemCallback<InstrumentModel>() {
        // Question 1: Is this the same item? → compare by ID
        override fun areItemsTheSame(oldItem: InstrumentModel, newItem: InstrumentModel) = oldItem.id == newItem.id
        // Question 2: Did the content change? → data class compares all fields with ==
        override fun areContentsTheSame(oldItem: InstrumentModel, newItem: InstrumentModel) = oldItem == newItem
    }
}
