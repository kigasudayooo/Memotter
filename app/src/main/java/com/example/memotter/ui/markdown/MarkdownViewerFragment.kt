package com.example.memotter.ui.markdown

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.memotter.databinding.FragmentMarkdownViewerBinding
import com.example.memotter.util.FileManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MarkdownViewerFragment : Fragment() {

    private var _binding: FragmentMarkdownViewerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var fileManager: FileManager
    private var currentFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarkdownViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        fileManager = FileManager(requireContext())
        setupToolbar()
        setupViews()
        loadFileFromArguments()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupViews() {
        binding.fabUseTemplate.setOnClickListener {
            useTemplate()
        }
    }

    private fun loadFileFromArguments() {
        // For now, we'll get the file path from arguments
        // In a real implementation, you'd pass the file path via navigation arguments
        val arguments = arguments
        val filePath = arguments?.getString("file_path")
        
        if (filePath != null) {
            val file = File(filePath)
            loadFile(file)
        } else {
            // For demo purposes, show a sample file
            showSampleContent()
        }
    }

    private fun loadFile(file: File) {
        currentFile = file
        
        if (!file.exists()) {
            Toast.makeText(requireContext(), "ファイルが見つかりません", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        // Display file information
        binding.tvFileName.text = file.name
        
        val fileSize = "${file.length() / 1024}KB"
        val lastModified = SimpleDateFormat(
            "yyyy/MM/dd HH:mm", 
            Locale.getDefault()
        ).format(Date(file.lastModified()))
        
        binding.tvFileInfo.text = "$fileSize • 最終更新: $lastModified"

        // Read and display file content
        val content = fileManager.readMarkdownFile(file)
        if (content != null) {
            binding.tvMarkdownContent.text = content
            binding.toolbar.title = file.nameWithoutExtension
        } else {
            binding.tvMarkdownContent.text = "ファイルの読み取りに失敗しました"
            Toast.makeText(requireContext(), "ファイルの読み取りに失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSampleContent() {
        binding.tvFileName.text = "sample_template.md"
        binding.tvFileInfo.text = "1KB • 最終更新: 2024/06/16 12:30"
        binding.toolbar.title = "サンプルテンプレート"
        
        val sampleContent = """# 日記テンプレート

## 今日の日付
${SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date())}

## 天気
☀️ 晴れ / ☁️ 曇り / 🌧️ 雨 / ❄️ 雪

## 今日の気分
😊 良い / 😐 普通 / 😔 悪い

## 今日あったこと


## 今日学んだこと


## 明日の予定


## 感謝していること


---
*このテンプレートを使って日記を書いてみましょう！*"""

        binding.tvMarkdownContent.text = sampleContent
    }

    private fun useTemplate() {
        currentFile?.let { file ->
            val content = fileManager.readMarkdownFile(file)
            if (content != null) {
                // Navigate to memo fragment with template content
                // For now, show a toast
                Toast.makeText(
                    requireContext(),
                    "テンプレート「${file.nameWithoutExtension}」を新しいメモで使用します",
                    Toast.LENGTH_LONG
                ).show()
                
                // Navigate to new memo fragment
                // findNavController().navigate(MarkdownViewerFragmentDirections.actionToMemoFragment(content))
                findNavController().navigateUp()
            }
        } ?: run {
            Toast.makeText(requireContext(), "テンプレートファイルが選択されていません", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}