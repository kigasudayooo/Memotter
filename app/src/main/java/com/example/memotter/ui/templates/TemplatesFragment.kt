package com.example.memotter.ui.templates

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.R
import com.example.memotter.databinding.FragmentTemplatesBinding
import com.example.memotter.util.FileManager
import java.io.File

class TemplatesFragment : Fragment() {

    private var _binding: FragmentTemplatesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var fileManager: FileManager
    private lateinit var templateAdapter: TemplateAdapter

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadTemplates()
        } else {
            Toast.makeText(requireContext(), "ストレージ権限が必要です", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        fileManager = FileManager(requireContext())
        setupRecyclerView()
        checkPermissionAndLoadTemplates()
    }

    private fun setupRecyclerView() {
        templateAdapter = TemplateAdapter { templateFile ->
            // Handle template selection
            onTemplateSelected(templateFile)
        }
        
        binding.rvTemplates.apply {
            adapter = templateAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun checkPermissionAndLoadTemplates() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadTemplates()
            }
            else -> {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun loadTemplates() {
        if (!fileManager.isExternalStorageReadable()) {
            Toast.makeText(requireContext(), "外部ストレージが読み取れません", Toast.LENGTH_SHORT).show()
            showEmptyState()
            return
        }

        val templateFiles = fileManager.getTemplateFiles()
        
        if (templateFiles.isEmpty()) {
            showEmptyState()
        } else {
            showTemplates(templateFiles)
        }
    }

    private fun showEmptyState() {
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.rvTemplates.visibility = View.GONE
    }

    private fun showTemplates(templateFiles: List<File>) {
        binding.layoutEmpty.visibility = View.GONE
        binding.rvTemplates.visibility = View.VISIBLE
        templateAdapter.submitList(templateFiles)
    }

    private fun onTemplateSelected(templateFile: File) {
        val content = fileManager.readMarkdownFile(templateFile)
        if (content != null) {
            // Navigate to markdown viewer fragment
            val bundle = Bundle().apply {
                putString("file_path", templateFile.absolutePath)
            }
            findNavController().navigate(R.id.nav_markdown_viewer, bundle)
        } else {
            Toast.makeText(requireContext(), "ファイルの読み取りに失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}