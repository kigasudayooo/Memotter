package com.example.memotter.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memotter.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DirectoryAdapter(
    private val onDirectoryClick: (File) -> Unit
) : ListAdapter<File, DirectoryAdapter.DirectoryViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_directory, parent, false)
        return DirectoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        val directory = getItem(position)
        holder.bind(directory)
    }

    inner class DirectoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_directory_icon)
        private val tvName: TextView = itemView.findViewById(R.id.tv_directory_name)
        private val tvInfo: TextView = itemView.findViewById(R.id.tv_directory_info)

        fun bind(directory: File) {
            tvName.text = directory.name
            
            val lastModified = dateFormat.format(Date(directory.lastModified()))
            val fileCount = try {
                directory.listFiles()?.size ?: 0
            } catch (e: SecurityException) {
                0
            }
            
            tvInfo.text = "$lastModified • $fileCount 件"

            // アイコンを設定
            ivIcon.setImageResource(
                if (directory.canWrite()) {
                    R.drawable.ic_folder
                } else {
                    R.drawable.ic_folder_locked
                }
            )

            itemView.setOnClickListener {
                onDirectoryClick(directory)
            }

            // 書き込み権限がない場合はグレーアウト
            val alpha = if (directory.canWrite()) 1.0f else 0.5f
            itemView.alpha = alpha
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem.absolutePath == newItem.absolutePath
            }

            override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem.lastModified() == newItem.lastModified() &&
                        oldItem.canWrite() == newItem.canWrite()
            }
        }
    }
}