package com.example.memotter.ui.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memotter.data.model.DocumentInfo
import com.example.memotter.databinding.ItemDocumentSelectorBinding
import java.text.SimpleDateFormat
import java.util.*

class DocumentSelectorAdapter(
    private val onDocumentClick: (DocumentInfo) -> Unit
) : ListAdapter<DocumentInfo, DocumentSelectorAdapter.DocumentViewHolder>(DocumentDiffCallback()) {

    private var selectedDocument: DocumentInfo? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemDocumentSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSelectedDocument(document: DocumentInfo) {
        val previousSelected = selectedDocument
        selectedDocument = document
        
        // Update previous selection
        previousSelected?.let { prev ->
            val prevIndex = currentList.indexOf(prev)
            if (prevIndex != -1) {
                notifyItemChanged(prevIndex)
            }
        }
        
        // Update new selection
        val newIndex = currentList.indexOf(document)
        if (newIndex != -1) {
            notifyItemChanged(newIndex)
        }
    }

    fun clearSelection() {
        val previousSelected = selectedDocument
        selectedDocument = null
        
        previousSelected?.let { prev ->
            val prevIndex = currentList.indexOf(prev)
            if (prevIndex != -1) {
                notifyItemChanged(prevIndex)
            }
        }
    }

    inner class DocumentViewHolder(
        private val binding: ItemDocumentSelectorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(document: DocumentInfo) {
            binding.apply {
                tvDocumentName.text = document.name
                
                // Format file info
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                val now = Date()
                val isToday = isSameDay(document.lastModified, now)
                
                val timeStr = if (isToday) {
                    "今日 ${timeFormat.format(document.lastModified)}"
                } else {
                    "${dateFormat.format(document.lastModified)} ${timeFormat.format(document.lastModified)}"
                }
                
                tvDocumentInfo.text = "${document.formattedSize} • $timeStr"
                tvDocumentPreview.text = document.preview
                
                // Set selection state
                rbSelected.isChecked = document == selectedDocument
                
                // Set click listener
                root.setOnClickListener {
                    onDocumentClick(document)
                }
            }
        }

        private fun isSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = Calendar.getInstance().apply { time = date1 }
            val cal2 = Calendar.getInstance().apply { time = date2 }
            
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }
    }

    private class DocumentDiffCallback : DiffUtil.ItemCallback<DocumentInfo>() {
        override fun areItemsTheSame(oldItem: DocumentInfo, newItem: DocumentInfo): Boolean {
            return oldItem.file.absolutePath == newItem.file.absolutePath
        }

        override fun areContentsTheSame(oldItem: DocumentInfo, newItem: DocumentInfo): Boolean {
            return oldItem.name == newItem.name &&
                   oldItem.size == newItem.size &&
                   oldItem.lastModified == newItem.lastModified &&
                   oldItem.preview == newItem.preview
        }
    }
}