package com.example.memotter.ui.templates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memotter.databinding.ItemTemplateBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TemplateAdapter(
    private val onTemplateClick: (File) -> Unit
) : ListAdapter<File, TemplateAdapter.TemplateViewHolder>(TemplateDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val binding = ItemTemplateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TemplateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TemplateViewHolder(
        private val binding: ItemTemplateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(templateFile: File) {
            binding.apply {
                // Set template file name (without .md extension)
                val fileName = templateFile.nameWithoutExtension
                tvTemplateName.text = fileName

                // Set file size and last modified date
                val fileSize = "${templateFile.length() / 1024}KB"
                val lastModified = SimpleDateFormat(
                    "yyyy/MM/dd HH:mm", 
                    Locale.getDefault()
                ).format(Date(templateFile.lastModified()))
                
                tvTemplateInfo.text = "$fileSize • $lastModified"

                // Try to read first few lines as preview
                try {
                    val content = templateFile.readText(Charsets.UTF_8)
                    val preview = content.lines()
                        .take(3)
                        .joinToString("\n")
                        .take(100)
                    
                    tvTemplatePreview.text = if (preview.length >= 100) {
                        preview + "..."
                    } else {
                        preview
                    }
                } catch (e: Exception) {
                    tvTemplatePreview.text = "プレビューできません"
                }

                // Set click listener
                root.setOnClickListener {
                    onTemplateClick(templateFile)
                }
            }
        }
    }

    private class TemplateDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.absolutePath == newItem.absolutePath
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.lastModified() == newItem.lastModified() && 
                   oldItem.length() == newItem.length()
        }
    }
}