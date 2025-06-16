package com.example.memotter.ui.markdown

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.R
import com.example.memotter.data.model.Memo
import com.example.memotter.databinding.FragmentMarkdownViewerBinding
import com.example.memotter.ui.dialog.FileEditDialog
import com.example.memotter.ui.dialog.MemoEditDialog
import com.example.memotter.ui.timeline.MemoAdapter
import com.example.memotter.util.CurrentFileManager
import com.example.memotter.util.FileManager
import com.example.memotter.util.MarkdownFileParser
import io.noties.markwon.Markwon
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MarkdownViewerFragment : Fragment() {

    private var _binding: FragmentMarkdownViewerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var fileManager: FileManager
    private lateinit var currentFileManager: CurrentFileManager
    private lateinit var markdownFileParser: MarkdownFileParser
    private lateinit var markwon: Markwon
    private lateinit var memoAdapter: MemoAdapter
    
    private var currentFile: File? = null
    private var currentMemos: List<Memo> = emptyList()
    private var isTimelineMode = false

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
        currentFileManager = CurrentFileManager(requireContext())
        markdownFileParser = MarkdownFileParser()
        markwon = Markwon.create(requireContext())
        
        setupMenu()
        setupToolbar()
        setupRecyclerView()
        setupViews()
        loadFileFromArguments()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.markdown_viewer, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_toggle_mode -> {
                        toggleViewMode()
                        true
                    }
                    R.id.action_edit_file -> {
                        openFileEditor()
                        true
                    }
                    R.id.action_save_file -> {
                        saveCurrentFile()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // 現在のファイル状態をクリア
            currentFileManager.clearCurrentFile()
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        memoAdapter = MemoAdapter(
            onMemoClick = { memo ->
                editMemo(memo)
            },
            onFavoriteClick = { memo ->
                // お気に入り機能は今後実装
                Toast.makeText(requireContext(), "お気に入り機能は今後実装予定です", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvTimeline.apply {
            adapter = memoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupViews() {
        binding.fabAction.setOnClickListener {
            if (isTimelineMode) {
                addNewMemo()
            } else {
                useTemplate()
            }
        }
    }

    private fun loadFileFromArguments() {
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

    private fun toggleViewMode() {
        isTimelineMode = !isTimelineMode
        updateViewMode()
    }

    private fun updateViewMode() {
        if (isTimelineMode) {
            // タイムラインモードに切り替え
            binding.scrollViewMode.visibility = View.GONE
            binding.layoutTimelineMode.visibility = View.VISIBLE
            binding.toolbar.title = "${currentFile?.nameWithoutExtension ?: "ファイル"} - タイムライン"
            binding.fabAction.contentDescription = "新しいメモを追加"
            
            // メモリストを表示
            displayTimelineMode()
        } else {
            // 表示モードに切り替え
            binding.scrollViewMode.visibility = View.VISIBLE
            binding.layoutTimelineMode.visibility = View.GONE
            binding.toolbar.title = currentFile?.nameWithoutExtension ?: "マークダウンビューア"
            binding.fabAction.contentDescription = "テンプレートを使用"
        }
    }

    private fun displayTimelineMode() {
        if (currentMemos.isEmpty()) {
            binding.rvTimeline.visibility = View.GONE
            binding.layoutEmptyTimeline.visibility = View.VISIBLE
        } else {
            binding.rvTimeline.visibility = View.VISIBLE
            binding.layoutEmptyTimeline.visibility = View.GONE
            memoAdapter.submitList(currentMemos)
        }

        binding.tvTimelineFileName.text = currentFile?.name ?: ""
        binding.tvMemoCount.text = "${currentMemos.size}件のメモ"
    }

    private fun addNewMemo() {
        // 現在のファイルを設定
        currentFile?.let { file ->
            currentFileManager.setCurrentFile(file)
            currentFileManager.isEditMode = true
        }

        // メモ作成画面に遷移
        findNavController().navigate(R.id.nav_new_memo)
    }

    private fun openFileEditor() {
        currentFile?.let { file ->
            val content = fileManager.readMarkdownFile(file) ?: ""
            val dialog = FileEditDialog.newInstance(file, content) { newContent ->
                onFileContentChanged(newContent)
            }
            dialog.show(parentFragmentManager, "FileEditDialog")
        }
    }

    private fun onFileContentChanged(newContent: String) {
        currentFile?.let { file ->
            // ファイルに保存
            val success = fileManager.writeMarkdownFile(file, newContent)
            
            if (success) {
                // 表示を更新
                refreshFileDisplay(file, newContent)
                Toast.makeText(requireContext(), "ファイルが更新されました", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "ファイルの保存に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshFileDisplay(file: File, content: String) {
        // ファイル情報を更新
        updateFileInfo(file)
        
        // マークダウン表示を更新
        markwon.setMarkdown(binding.tvMarkdownContent, content)
        
        // タイムライン表示用にパース
        currentMemos = markdownFileParser.parseFileToMemos(content, file.absolutePath)
        
        // 現在のモードに応じて表示を更新
        if (isTimelineMode) {
            displayTimelineMode()
        }
    }

    private fun saveCurrentFile() {
        currentFile?.let { file ->
            val content = markdownFileParser.memosToFileContent(currentMemos)
            val success = fileManager.writeMarkdownFile(file, content)
            
            if (success) {
                Toast.makeText(requireContext(), "ファイルを保存しました", Toast.LENGTH_SHORT).show()
                // ファイル情報を更新
                updateFileInfo(file)
            } else {
                Toast.makeText(requireContext(), "ファイルの保存に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editMemo(memo: Memo) {
        val dialog = MemoEditDialog.newInstance(memo) { newContent ->
            updateMemoInFile(memo, newContent)
        }
        dialog.show(parentFragmentManager, "MemoEditDialog")
    }

    private fun updateMemoInFile(originalMemo: Memo, newContent: String) {
        currentFile?.let { file ->
            // メモリストを更新
            val updatedMemos = currentMemos.map { memo ->
                if (memo.id == originalMemo.id) {
                    memo.copy(
                        content = newContent,
                        updatedAt = Date(),
                        hashtags = com.example.memotter.util.HashtagExtractor.extractHashtags(newContent).joinToString(",")
                    )
                } else {
                    memo
                }
            }
            
            // ファイルに保存
            val newFileContent = markdownFileParser.memosToFileContent(updatedMemos)
            val success = fileManager.writeMarkdownFile(file, newFileContent)
            
            if (success) {
                // 表示を更新
                currentMemos = updatedMemos
                refreshFileDisplay(file, newFileContent)
                Toast.makeText(requireContext(), "メモを更新しました", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "メモの更新に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFile(file: File) {
        currentFile = file
        
        if (!file.exists()) {
            Toast.makeText(requireContext(), "ファイルが見つかりません", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        // 現在のファイルとして設定
        currentFileManager.setCurrentFile(file)

        // ファイル情報を表示
        updateFileInfo(file)

        // ファイル内容を読み込み
        val content = fileManager.readMarkdownFile(file)
        if (content != null) {
            // マークダウン表示用
            markwon.setMarkdown(binding.tvMarkdownContent, content)
            binding.toolbar.title = file.nameWithoutExtension
            
            // タイムライン表示用にパース
            currentMemos = markdownFileParser.parseFileToMemos(content, file.absolutePath)
            
            // 初期表示モードを設定
            updateViewMode()
        } else {
            binding.tvMarkdownContent.text = "ファイルの読み取りに失敗しました"
            Toast.makeText(requireContext(), "ファイルの読み取りに失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFileInfo(file: File) {
        binding.tvFileName.text = file.name
        
        val fileSize = "${file.length() / 1024}KB"
        val lastModified = SimpleDateFormat(
            "yyyy/MM/dd HH:mm", 
            Locale.getDefault()
        ).format(Date(file.lastModified()))
        
        binding.tvFileInfo.text = "$fileSize • 最終更新: $lastModified"
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

        markwon.setMarkdown(binding.tvMarkdownContent, sampleContent)
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