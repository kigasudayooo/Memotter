package com.example.memotter.ui.memo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.R
import com.example.memotter.data.database.MemotterDatabase
import com.example.memotter.data.repository.MemoRepository
import com.example.memotter.databinding.FragmentMemoBinding
import com.example.memotter.data.model.Document
import com.example.memotter.ui.dialog.DocumentSelectorDialog
import com.example.memotter.ui.dialog.SaveDocumentDialog
import com.example.memotter.util.DocumentManager
import io.noties.markwon.Markwon
import java.util.Locale

class MemoFragment : Fragment() {

    private var _binding: FragmentMemoBinding? = null
    private val binding get() = _binding!!

    private lateinit var memoViewModel: MemoViewModel
    private lateinit var hashtagAdapter: HashtagSuggestionAdapter
    private lateinit var documentManager: DocumentManager
    private lateinit var markwon: Markwon
    private var currentDocument: Document? = null

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val spokenText = results[0]
                val currentText = binding.etContent.text.toString()
                val newText = if (currentText.isEmpty()) spokenText else "$currentText $spokenText"
                binding.etContent.setText(newText)
                binding.etContent.setSelection(newText.length)
            }
        }
    }

    private val microphonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceInput()
        } else {
            Toast.makeText(requireContext(), "音声入力には音声録音権限が必要です", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupViews()
        setupHashtagSuggestions()
        observeData()
    }

    private fun setupViewModel() {
        val database = MemotterDatabase.getDatabase(requireContext())
        val repository = MemoRepository(database.memoDao(), database.hashtagDao(), requireContext())
        val factory = MemoViewModelFactory(repository)
        memoViewModel = ViewModelProvider(this, factory)[MemoViewModel::class.java]
        
        // Initialize document manager
        documentManager = DocumentManager(requireContext())
        
        // Initialize Markwon
        markwon = Markwon.create(requireContext())
        
        // Initialize with empty state
        updateDocumentTitle()
    }

    private fun setupViews() {
        binding.apply {
            // Text change listener for content
            etContent.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val content = s.toString()
                    btnSave.isEnabled = content.trim().isNotEmpty()
                    updateStatusText(content)
                    memoViewModel.onContentChanged(content)
                    
                    // Update markdown preview
                    updateMarkdownPreview(content)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // Document title click - show document selector
            tvDocumentTitle.setOnClickListener {
                showDocumentSelector()
            }

            // Save button click
            btnSave.setOnClickListener {
                val content = etContent.text.toString().trim()
                if (content.isNotEmpty()) {
                    memoViewModel.saveMemo(content)
                }
            }

            // Save as button click
            btnSaveAs.setOnClickListener {
                showSaveAsDialog()
            }

            // Voice input button click
            btnVoiceInput.setOnClickListener {
                checkMicrophonePermissionAndStartVoiceInput()
            }
        }
    }

    private fun setupHashtagSuggestions() {
        hashtagAdapter = HashtagSuggestionAdapter { hashtag ->
            insertHashtag(hashtag)
        }

        binding.rvHashtagSuggestions.apply {
            adapter = hashtagAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun observeData() {
        memoViewModel.hashtags.observe(viewLifecycleOwner) { hashtags ->
            if (hashtags.isNotEmpty()) {
                hashtagAdapter.submitList(hashtags.take(10)) // Show top 10 suggestions
                binding.hashtagSuggestionsLayout.visibility = View.VISIBLE
            } else {
                binding.hashtagSuggestionsLayout.visibility = View.GONE
            }
        }

        memoViewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "メモを保存しました", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "メモの保存に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatusText(content: String) {
        val charCount = content.length
        binding.statusText.text = "文字数: $charCount"
    }

    private fun insertHashtag(hashtag: String) {
        val currentText = binding.etContent.text.toString()
        val cursorPosition = binding.etContent.selectionStart
        val newText = StringBuilder(currentText).insert(cursorPosition, "#$hashtag ").toString()
        binding.etContent.setText(newText)
        binding.etContent.setSelection(cursorPosition + hashtag.length + 2)
    }

    private fun checkMicrophonePermissionAndStartVoiceInput() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceInput()
            }
            else -> {
                microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Toast.makeText(requireContext(), "音声認識が利用できません", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ja-JP")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "メモを音声で入力してください")
        }

        speechRecognizerLauncher.launch(intent)
    }

    private fun showDocumentSelector() {
        val dialog = DocumentSelectorDialog.newInstance(
            onDocumentSelected = { file ->
                openDocument(file)
            },
            onNewDocumentRequested = {
                createNewDocument()
            }
        )
        dialog.show(parentFragmentManager, "DocumentSelector")
    }

    private fun showSaveAsDialog() {
        currentDocument?.let { doc ->
            val dialog = SaveDocumentDialog.newInstance(
                document = doc,
                onSaveSuccess = { file ->
                    Toast.makeText(requireContext(), "ファイルを保存しました: ${file.name}", Toast.LENGTH_SHORT).show()
                    updateDocumentTitle()
                },
                onSaveError = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            )
            dialog.show(parentFragmentManager, "SaveDocument")
        }
    }

    private fun openDocument(file: java.io.File) {
        val document = documentManager.openDocument(file)
        if (document != null) {
            currentDocument = document
            binding.etContent.setText(document.content)
            updateDocumentTitle()
            Toast.makeText(requireContext(), "ドキュメントを開きました: ${document.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "ファイルの読み込みに失敗しました", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNewDocument() {
        currentDocument = documentManager.createNewDocument()
        binding.etContent.setText("")
        updateDocumentTitle()
        Toast.makeText(requireContext(), "新しいドキュメントを作成しました", Toast.LENGTH_SHORT).show()
    }

    private fun saveCurrentDocument() {
        currentDocument?.let { doc ->
            if (documentManager.saveDocument(doc)) {
                currentDocument = documentManager.getCurrentDocument()
                updateDocumentTitle()
                Toast.makeText(requireContext(), "ドキュメントを保存しました", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "保存に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDocumentTitle() {
        val title = currentDocument?.let { doc ->
            if (doc.isModified) "${doc.displayName} *" else doc.displayName
        } ?: "新しいメモ"
        binding.tvDocumentTitle.text = title
    }

    private fun updateMarkdownPreview(content: String) {
        if (content.isEmpty()) {
            binding.tvPreview.text = "プレビューがここに表示されます"
        } else {
            markwon.setMarkdown(binding.tvPreview, content)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}