package com.example.memotter.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.DialogFragment
import com.example.memotter.R
import com.example.memotter.data.model.Document
import com.example.memotter.databinding.DialogSaveDocumentBinding
import com.example.memotter.util.DocumentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class SaveDocumentDialog : DialogFragment() {

    private var _binding: DialogSaveDocumentBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var documentManager: DocumentManager
    private var document: Document? = null
    private var onSaveSuccess: ((File) -> Unit)? = null
    private var onSaveError: ((String) -> Unit)? = null
    
    companion object {
        fun newInstance(
            document: Document,
            onSaveSuccess: (File) -> Unit,
            onSaveError: (String) -> Unit
        ): SaveDocumentDialog {
            return SaveDocumentDialog().apply {
                this.document = document
                this.onSaveSuccess = onSaveSuccess
                this.onSaveError = onSaveError
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSaveDocumentBinding.inflate(layoutInflater)
        
        documentManager = DocumentManager(requireContext())
        setupViews()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupViews() {
        setupFileNameInput()
        setupSavePath()
        setupButtons()
        updateSaveButtonState()
    }

    private fun setupFileNameInput() {
        // Set default filename
        val defaultName = document?.name?.takeIf { it != "新しいドキュメント" } 
            ?: generateDefaultFileName()
        
        binding.etFilename.setText(defaultName)
        binding.etFilename.selectAll()
        
        // Add text watcher for validation
        binding.etFilename.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateFileName()
                updateSaveButtonState()
            }
        })
    }

    private fun setupSavePath() {
        val savePath = documentManager.fileManager.getMemotterDirectory().absolutePath
        binding.tvSavePath.text = savePath
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnSave.setOnClickListener {
            saveDocument()
        }
    }

    private fun generateDefaultFileName(): String {
        val baseName = "新しいドキュメント"
        return documentManager.getDuplicateFileName(baseName)
    }

    private fun validateFileName(): Boolean {
        val fileName = binding.etFilename.text.toString().trim()
        val inputLayout = binding.etFilename.parent.parent as com.google.android.material.textfield.TextInputLayout
        
        when {
            fileName.isBlank() -> {
                inputLayout.error = "ファイル名を入力してください"
                return false
            }
            !isValidFileName(fileName) -> {
                inputLayout.error = "使用できない文字が含まれています"
                return false
            }
            fileExists(fileName) && !binding.switchOverwrite.isChecked -> {
                inputLayout.error = "同名のファイルが存在します"
                return false
            }
            else -> {
                inputLayout.error = null
                return true
            }
        }
    }

    private fun isValidFileName(fileName: String): Boolean {
        val invalidChars = listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        return !invalidChars.any { fileName.contains(it) }
    }

    private fun fileExists(fileName: String): Boolean {
        val extension = when (binding.rgFileType.checkedRadioButtonId) {
            R.id.rb_text -> ".txt"
            else -> ".md"
        }
        val file = File(documentManager.fileManager.getMemotterDirectory(), fileName + extension)
        return file.exists()
    }

    private fun updateSaveButtonState() {
        binding.btnSave.isEnabled = validateFileName()
    }

    private fun saveDocument() {
        val doc = document ?: return
        val fileName = binding.etFilename.text.toString().trim()
        
        if (!validateFileName()) {
            return
        }
        
        // Add appropriate extension
        val extension = when (binding.rgFileType.checkedRadioButtonId) {
            R.id.rb_text -> ".txt"
            else -> ".md"
        }
        
        val fullFileName = if (fileName.endsWith(".md") || fileName.endsWith(".txt")) {
            fileName
        } else {
            fileName + extension
        }
        
        // Check for overwrite
        val targetFile = File(documentManager.fileManager.getMemotterDirectory(), fullFileName)
        if (targetFile.exists() && !binding.switchOverwrite.isChecked) {
            onSaveError?.invoke("同名のファイルが存在します。上書きを許可するか、別の名前を選択してください。")
            return
        }
        
        // Save the document
        val savedFile = documentManager.saveDocumentAs(doc, fileName)
        if (savedFile != null) {
            onSaveSuccess?.invoke(savedFile)
            dismiss()
        } else {
            onSaveError?.invoke("ファイルの保存に失敗しました。")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}