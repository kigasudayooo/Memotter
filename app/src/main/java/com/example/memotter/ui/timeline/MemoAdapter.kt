package com.example.memotter.ui.timeline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memotter.R
import com.example.memotter.data.model.Memo
import com.example.memotter.databinding.ItemMemoBinding
import com.example.memotter.util.HashtagExtractor
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Locale

class MemoAdapter(
    private val onMemoClick: (Memo) -> Unit,
    private val onFavoriteClick: (Memo) -> Unit
) : ListAdapter<Memo, MemoAdapter.MemoViewHolder>(DiffCallback) {

    private var markwon: Markwon? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = ItemMemoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        
        // Initialize Markwon if not already initialized
        if (markwon == null) {
            markwon = Markwon.create(parent.context)
        }
        
        return MemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = getItem(position)
        holder.bind(memo)
    }

    inner class MemoViewHolder(
        private val binding: ItemMemoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(memo: Memo) {
            binding.apply {
                // Format timestamp
                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                tvTimestamp.text = dateFormat.format(memo.createdAt)

                // Set content with markdown rendering
                markwon?.setMarkdown(tvContent, memo.content)

                // Set hashtags if any
                if (memo.hashtags.isNotEmpty()) {
                    tvHashtags.text = memo.hashtags.split(",").joinToString(" ") { "#$it" }
                    tvHashtags.visibility = android.view.View.VISIBLE
                } else {
                    tvHashtags.visibility = android.view.View.GONE
                }

                // Set favorite button state
                btnFavorite.setImageResource(
                    if (memo.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                )

                // Set click listeners
                root.setOnClickListener { onMemoClick(memo) }
                btnFavorite.setOnClickListener { onFavoriteClick(memo) }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Memo>() {
            override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean {
                return oldItem == newItem
            }
        }
    }
}