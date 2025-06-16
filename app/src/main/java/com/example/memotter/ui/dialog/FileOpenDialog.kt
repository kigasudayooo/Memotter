package com.example.memotter.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memotter.R
import com.example.memotter.util.FileManager
import java.io.File

class FileOpenDialog : DialogFragment() {

    private var onFileSelectedListener: ((File) -> Unit)? = null

    companion object {
        fun newInstance(onFileSelected: (File) -> Unit): FileOpenDialog {
            return FileOpenDialog().apply {
                onFileSelectedListener = onFileSelected
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val fileManager = FileManager(requireContext())
        val memoFiles = fileManager.getMemoFiles()

        val adapter = FileListAdapter(memoFiles) { file ->
            onFileSelectedListener?.invoke(file)
            dismiss()
        }

        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("ファイルを選択")
            .setView(recyclerView)
            .setNegativeButton("キャンセル") { _, _ ->
                dismiss()
            }
            .create()
    }
}