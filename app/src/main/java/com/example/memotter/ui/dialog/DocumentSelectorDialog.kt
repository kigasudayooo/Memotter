package com.example.memotter.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.R
import com.example.memotter.data.model.DocumentInfo
import com.example.memotter.databinding.DialogDocumentSelectorBinding
import com.example.memotter.util.DocumentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import java.io.File

class DocumentSelectorDialog : DialogFragment() {

    private var _binding: DialogDocumentSelectorBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var documentManager: DocumentManager
    private lateinit var adapter: DocumentSelectorAdapter
    private var selectedDocument: DocumentInfo? = null
    private var onDocumentSelected: ((File) -> Unit)? = null
    private var onNewDocumentRequested: (() -> Unit)? = null
    
    companion object {
        fun newInstance(
            onDocumentSelected: (File) -> Unit,
            onNewDocumentRequested: () -> Unit
        ): DocumentSelectorDialog {
            return DocumentSelectorDialog().apply {
                this.onDocumentSelected = onDocumentSelected
                this.onNewDocumentRequested = onNewDocumentRequested
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDocumentSelectorBinding.inflate(layoutInflater)
        
        documentManager = DocumentManager(requireContext())
        setupViews()
        loadDocuments()
        
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupViews() {
        setupRecyclerView()
        setupSearch()
        setupTabs()
        setupButtons()
    }

    private fun setupRecyclerView() {
        adapter = DocumentSelectorAdapter { document ->
            selectedDocument = document
            binding.btnSelect.isEnabled = true
            // Update selection state in adapter
            adapter.setSelectedDocument(document)
        }
        
        binding.rvDocuments.apply {
            adapter = this@DocumentSelectorDialog.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    loadDocuments()
                } else {
                    searchDocuments(query)
                }
            }
        })
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadDocuments() // すべて
                    1 -> loadTodayDocuments() // 今日
                    2 -> loadRecentDocuments() // 最近
                }
                clearSelection()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnSelect.setOnClickListener {
            selectedDocument?.let { document ->
                onDocumentSelected?.invoke(document.file)
                dismiss()
            }
        }
        
        binding.btnNewDocument.setOnClickListener {
            onNewDocumentRequested?.invoke()
            dismiss()
        }
    }

    private fun loadDocuments() {
        val documents = documentManager.getAllDocuments()
        adapter.submitList(documents)
        updateEmptyState(documents.isEmpty())
    }

    private fun loadTodayDocuments() {
        val documents = documentManager.getTodayDocuments()
        adapter.submitList(documents)
        updateEmptyState(documents.isEmpty())
    }

    private fun loadRecentDocuments() {
        val documents = documentManager.getRecentDocuments(7)
        adapter.submitList(documents)
        updateEmptyState(documents.isEmpty())
    }

    private fun searchDocuments(query: String) {
        val documents = documentManager.searchDocuments(query)
        adapter.submitList(documents)
        updateEmptyState(documents.isEmpty())
    }

    private fun clearSelection() {
        selectedDocument = null
        binding.btnSelect.isEnabled = false
        adapter.clearSelection()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            // Show empty state message
            binding.rvDocuments.visibility = View.GONE
            // You could add an empty state view here
        } else {
            binding.rvDocuments.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}