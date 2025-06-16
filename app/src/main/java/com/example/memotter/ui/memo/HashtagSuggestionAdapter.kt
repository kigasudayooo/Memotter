package com.example.memotter.ui.memo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memotter.data.model.Hashtag
import com.example.memotter.databinding.ItemHashtagSuggestionBinding

class HashtagSuggestionAdapter(
    private val onHashtagClick: (String) -> Unit
) : ListAdapter<Hashtag, HashtagSuggestionAdapter.HashtagViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagViewHolder {
        val binding = ItemHashtagSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HashtagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HashtagViewHolder, position: Int) {
        val hashtag = getItem(position)
        holder.bind(hashtag)
    }

    inner class HashtagViewHolder(
        private val binding: ItemHashtagSuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hashtag: Hashtag) {
            binding.apply {
                chipHashtag.text = "#${hashtag.tag}"
                chipHashtag.setOnClickListener {
                    onHashtagClick(hashtag.tag)
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Hashtag>() {
            override fun areItemsTheSame(oldItem: Hashtag, newItem: Hashtag): Boolean {
                return oldItem.tag == newItem.tag
            }

            override fun areContentsTheSame(oldItem: Hashtag, newItem: Hashtag): Boolean {
                return oldItem == newItem
            }
        }
    }
}