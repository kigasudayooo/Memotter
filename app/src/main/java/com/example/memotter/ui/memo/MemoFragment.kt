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
import java.util.Locale

class MemoFragment : Fragment() {

    private var _binding: FragmentMemoBinding? = null
    private val binding get() = _binding!!

    private lateinit var memoViewModel: MemoViewModel
    private lateinit var hashtagAdapter: HashtagSuggestionAdapter

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
        val repository = MemoRepository(database.memoDao(), database.hashtagDao())
        val factory = MemoViewModelFactory(repository)
        memoViewModel = ViewModelProvider(this, factory)[MemoViewModel::class.java]
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
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // Save button click
            btnSave.setOnClickListener {
                val content = etContent.text.toString().trim()
                if (content.isNotEmpty()) {
                    memoViewModel.saveMemo(content)
                }
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
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "メモを音声で入力してください")
        }

        speechRecognizerLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}