package com.example.memotter.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.R
import com.example.memotter.data.model.Memo
import com.example.memotter.databinding.DialogFileEditBinding
import com.example.memotter.ui.timeline.MemoAdapter
import com.example.memotter.util.MarkdownFileParser
import io.noties.markwon.Markwon
import java.io.File

class FileEditDialog : DialogFragment() {

    private var _binding: DialogFileEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var markwon: Markwon
    private lateinit var markdownFileParser: MarkdownFileParser
    private lateinit var timelineAdapter: MemoAdapter

    private var file: File? = null
    private var originalContent: String = ""
    private var onSaveListener: ((String) -> Unit)? = null
    private var isTimelinePreview = false

    companion object {
        fun newInstance(
            file: File,
            content: String,
            onSave: (String) -> Unit
        ): FileEditDialog {
            return FileEditDialog().apply {
                this.file = file
                this.originalContent = content
                this.onSaveListener = onSave
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Memotter)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupComponents()
        setupToolbar()
        setupEditor()
        setupPreview()
        loadContent()
    }

    private fun setupComponents() {
        markwon = Markwon.create(requireContext())
        markdownFileParser = MarkdownFileParser()

        timelineAdapter = MemoAdapter(
            onMemoClick = { /* Preview only - no interaction */ },
            onFavoriteClick = { /* Preview only - no interaction */ }
        )

        binding.rvTimelinePreview.apply {
            adapter = timelineAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            checkForUnsavedChanges()
        }

        binding.toolbar.title = "編集: ${file?.nameWithoutExtension ?: "ファイル"}"
    }

    private fun setupEditor() {
        binding.etFileContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val content = s.toString()
                updateCharCount(content)
                updatePreview(content)
                updateSaveButton(content)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSave.setOnClickListener {
            saveFile()
        }
    }

    private fun setupPreview() {
        binding.btnTogglePreview.setOnCheckedChangeListener { _, isChecked ->
            isTimelinePreview = isChecked
            togglePreviewMode()
        }
    }

    private fun loadContent() {
        binding.etFileContent.setText(originalContent)
        updateCharCount(originalContent)
        updatePreview(originalContent)
        updateSaveButton(originalContent)
    }

    private fun updateCharCount(content: String) {
        val charCount = content.length
        val lineCount = content.split("\n").size
        binding.tvCharCount.text = "文字数: $charCount | 行数: $lineCount"
    }

    private fun updatePreview(content: String) {
        if (isTimelinePreview) {
            // タイムラインプレビュー
            val memos = try {
                markdownFileParser.parseFileToMemos(content, file?.absolutePath ?: "")
            } catch (e: Exception) {
                emptyList<Memo>()
            }
            timelineAdapter.submitList(memos)
        } else {
            // マークダウンプレビュー
            if (content.isEmpty()) {
                binding.tvMarkdownPreview.text = "プレビューがここに表示されます"
            } else {
                markwon.setMarkdown(binding.tvMarkdownPreview, content)
            }
        }
    }

    private fun togglePreviewMode() {
        if (isTimelinePreview) {
            binding.scrollMarkdownPreview.visibility = View.GONE
            binding.rvTimelinePreview.visibility = View.VISIBLE
        } else {
            binding.scrollMarkdownPreview.visibility = View.VISIBLE
            binding.rvTimelinePreview.visibility = View.GONE
        }
        
        // プレビューを更新
        updatePreview(binding.etFileContent.text.toString())
    }

    private fun updateSaveButton(content: String) {
        binding.btnSave.isEnabled = content != originalContent
    }

    private fun saveFile() {
        val newContent = binding.etFileContent.text.toString()
        onSaveListener?.invoke(newContent)
        originalContent = newContent
        updateSaveButton(newContent)
        Toast.makeText(requireContext(), "ファイルを保存しました", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun checkForUnsavedChanges() {
        val currentContent = binding.etFileContent.text.toString()
        if (currentContent != originalContent) {
            // 未保存の変更がある場合の確認ダイアログ
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("未保存の変更があります")
                .setMessage("変更を保存せずに閉じますか？")
                .setPositiveButton("保存して閉じる") { _, _ ->
                    saveFile()
                }
                .setNegativeButton("保存せず閉じる") { _, _ ->
                    dismiss()
                }
                .setNeutralButton("キャンセル") { _, _ ->
                    // 何もしない
                }
                .show()
        } else {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}