package com.example.memotter.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.R
import com.example.memotter.databinding.DialogDirectorySelectBinding
import java.io.File

class DirectorySelectDialog : DialogFragment() {

    private var _binding: DialogDirectorySelectBinding? = null
    private val binding get() = _binding!!

    private lateinit var directoryAdapter: DirectoryAdapter
    private var currentDirectory: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    private var onDirectorySelectedListener: ((String) -> Unit)? = null

    companion object {
        fun newInstance(onDirectorySelected: (String) -> Unit): DirectorySelectDialog {
            return DirectorySelectDialog().apply {
                onDirectorySelectedListener = onDirectorySelected
            }
        }
    }
    
    fun setOnDirectorySelectedListener(listener: (String) -> Unit) {
        onDirectorySelectedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDirectorySelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupRecyclerView()
        loadDirectory(currentDirectory)
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        binding.btnSelect.setOnClickListener {
            selectCurrentDirectory()
        }

        binding.btnParent.setOnClickListener {
            navigateToParent()
        }

        binding.btnCreateNew.setOnClickListener {
            showCreateDirectoryDialog()
        }
    }

    private fun setupRecyclerView() {
        directoryAdapter = DirectoryAdapter { directory ->
            if (directory.isDirectory) {
                loadDirectory(directory)
            }
        }

        binding.rvDirectories.apply {
            adapter = directoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadDirectory(directory: File) {
        if (!directory.exists() || !directory.isDirectory) {
            Toast.makeText(requireContext(), "ディレクトリにアクセスできません", Toast.LENGTH_SHORT).show()
            return
        }

        currentDirectory = directory
        binding.tvCurrentPath.text = directory.absolutePath

        // 親ディレクトリボタンの有効/無効
        binding.btnParent.isEnabled = directory.parent != null

        // ディレクトリ一覧を取得
        val directories = try {
            directory.listFiles { file ->
                file.isDirectory && !file.isHidden
            }?.sortedBy { it.name.lowercase() } ?: emptyList()
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "このディレクトリにアクセスする権限がありません", Toast.LENGTH_SHORT).show()
            emptyList()
        }

        directoryAdapter.submitList(directories)

        // 空の場合のメッセージ
        if (directories.isEmpty()) {
            binding.tvEmptyMessage.visibility = View.VISIBLE
            binding.rvDirectories.visibility = View.GONE
        } else {
            binding.tvEmptyMessage.visibility = View.GONE
            binding.rvDirectories.visibility = View.VISIBLE
        }
    }

    private fun navigateToParent() {
        currentDirectory.parentFile?.let { parent ->
            loadDirectory(parent)
        }
    }

    private fun selectCurrentDirectory() {
        if (!currentDirectory.canWrite()) {
            Toast.makeText(requireContext(), "このディレクトリは書き込み権限がありません", Toast.LENGTH_SHORT).show()
            return
        }

        onDirectorySelectedListener?.invoke(currentDirectory.absolutePath)
        dismiss()
    }

    private fun showCreateDirectoryDialog() {
        val input = android.widget.EditText(requireContext())
        input.hint = "新しいフォルダ名"

        AlertDialog.Builder(requireContext())
            .setTitle("新しいフォルダを作成")
            .setView(input)
            .setPositiveButton("作成") { _, _ ->
                val folderName = input.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    createNewDirectory(folderName)
                } else {
                    Toast.makeText(requireContext(), "フォルダ名を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun createNewDirectory(name: String) {
        val newDirectory = File(currentDirectory, name)
        
        if (newDirectory.exists()) {
            Toast.makeText(requireContext(), "同名のフォルダが既に存在します", Toast.LENGTH_SHORT).show()
            return
        }

        val success = try {
            newDirectory.mkdirs()
        } catch (e: SecurityException) {
            false
        }

        if (success) {
            Toast.makeText(requireContext(), "フォルダを作成しました", Toast.LENGTH_SHORT).show()
            loadDirectory(currentDirectory) // 一覧を更新
        } else {
            Toast.makeText(requireContext(), "フォルダの作成に失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                (resources.displayMetrics.heightPixels * 0.8).toInt()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}