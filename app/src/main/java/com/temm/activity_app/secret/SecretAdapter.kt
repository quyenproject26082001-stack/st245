package com.temm.activity_app.secret

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.temm.core.extensions.gone
import com.temm.core.extensions.visible
import com.temm.databinding.ItemSecretBinding

class SecretAdapter() : RecyclerView.Adapter<SecretAdapter.ViewHolder>() {

    var onItemClick: ((Secret, Int) -> Unit) = { _, _ -> }

    private val items = mutableListOf<Secret>()
    val currentList: List<Secret> get() = items

    inner class ViewHolder(val binding: ItemSecretBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: Secret) {
            binding.imgSecret.setImageResource(item.idImage)
            //binding.tvPlaySecret.text = item.nameState
            binding.root.setOnClickListener { onItemClick(item, bindingAdapterPosition) }
            if (item.isLocked == true) {
                binding.apply {
                    tvPlaySecret.gone()
                    tvUnlock.visible()
                    olItemSecret.visible()
                }

            } else {


                binding.tvPlaySecret.visible()
                binding.tvUnlock.gone()
                binding.olItemSecret.gone()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSecretBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.onBind(item)

    }


    fun submitList(newList: List<Secret>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
