package com.example.memotter.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.memotter.R
import com.example.memotter.data.model.Memo
import com.example.memotter.databinding.DialogMemoEditBinding
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.*

class MemoEditDialog : DialogFragment() {

    private var _binding: DialogMemoEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var markwon: Markwon
    private var memo: Memo? = null
    private var onSaveListener: ((String) -> Unit)? = null

    companion object {
        fun newInstance(
            memo: Memo,
            onSave: (String) -> Unit
        ): MemoEditDialog {
            return MemoEditDialog().apply {
                this.memo = memo
                this.onSaveListener = onSave
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Memotter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMemoEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        markwon = Markwon.create(requireContext())
        setupViews()
        loadMemoContent()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveMemo()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.etMemoContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreview(s.toString())
                updateCharCount(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadMemoContent() {
        memo?.let { memo ->
            binding.etMemoContent.setText(memo.content)
            
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            binding.tvMemoDate.text = "作成日時: ${dateFormat.format(memo.createdAt)}"
            
            binding.toolbar.title = "メモを編集"
            updatePreview(memo.content)
            updateCharCount(memo.content)
        }
    }

    private fun updatePreview(content: String) {
        if (content.isEmpty()) {
            binding.tvPreview.text = "プレビューがここに表示されます"
        } else {
            markwon.setMarkdown(binding.tvPreview, content)
        }
    }

    private fun updateCharCount(content: String) {
        binding.tvCharCount.text = "文字数: ${content.length}"
    }

    private fun saveMemo() {
        val newContent = binding.etMemoContent.text.toString().trim()
        
        if (newContent.isEmpty()) {
            Toast.makeText(requireContext(), "メモの内容を入力してください", Toast.LENGTH_SHORT).show()
            return
        }

        onSaveListener?.invoke(newContent)
        Toast.makeText(requireContext(), "メモを更新しました", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                (resources.displayMetrics.heightPixels * 0.8).toInt()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}