package com.example.memotter.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.data.database.MemotterDatabase
import com.example.memotter.data.repository.MemoRepository
import com.example.memotter.databinding.FragmentSearchBinding
import com.example.memotter.ui.memo.HashtagSuggestionAdapter
import com.example.memotter.ui.timeline.MemoAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var memoAdapter: MemoAdapter
    private lateinit var hashtagAdapter: HashtagSuggestionAdapter

    private var currentSearchType = SearchType.ALL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupViews()
        setupRecyclerViews()
        setupTabs()
        observeData()
    }

    private fun setupViewModel() {
        val database = MemotterDatabase.getDatabase(requireContext())
        val repository = MemoRepository(database.memoDao(), database.hashtagDao())
        val factory = SearchViewModelFactory(repository)
        searchViewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]
    }

    private fun setupViews() {
        binding.apply {
            // Search input
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s.toString().trim()
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    } else {
                        clearSearch()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            etSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = etSearch.text.toString().trim()
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setupRecyclerViews() {
        // Search results adapter
        memoAdapter = MemoAdapter(
            onMemoClick = { memo ->
                // TODO: Navigate to memo detail/edit
            },
            onFavoriteClick = { memo ->
                searchViewModel.toggleFavorite(memo.id, !memo.isFavorite)
            },
            onMoreClick = { memo ->
                // TODO: Show more options menu
            }
        )

        binding.rvSearchResults.apply {
            adapter = memoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Popular hashtags adapter
        hashtagAdapter = HashtagSuggestionAdapter { hashtag ->
            binding.etSearch.setText("#$hashtag")
            binding.etSearch.setSelection(hashtag.length + 1)
        }

        binding.rvPopularHashtags.apply {
            adapter = hashtagAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                currentSearchType = when (tab.position) {
                    0 -> SearchType.ALL
                    1 -> SearchType.HASHTAG
                    else -> SearchType.ALL
                }

                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    private fun observeData() {
        searchViewModel.searchResults.observe(viewLifecycleOwner) { memos ->
            memoAdapter.submitList(memos)
            updateSearchResultsVisibility(memos.isEmpty(), hasQuery = binding.etSearch.text.toString().isNotEmpty())
        }

        searchViewModel.popularHashtags.observe(viewLifecycleOwner) { hashtags ->
            hashtagAdapter.submitList(hashtags.take(10))
        }

        searchViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show loading indicator
        }
    }

    private fun performSearch(query: String) {
        when (currentSearchType) {
            SearchType.ALL -> searchViewModel.searchMemos(query)
            SearchType.HASHTAG -> {
                val cleanQuery = if (query.startsWith("#")) query.substring(1) else query
                searchViewModel.searchByHashtag(cleanQuery)
            }
        }
    }

    private fun clearSearch() {
        searchViewModel.clearSearch()
        updateSearchResultsVisibility(isEmpty = true, hasQuery = false)
    }

    private fun updateSearchResultsVisibility(isEmpty: Boolean, hasQuery: Boolean) {
        binding.apply {
            when {
                !hasQuery -> {
                    // No search query - show popular hashtags
                    rvSearchResults.visibility = View.GONE
                    layoutEmptySearch.visibility = View.VISIBLE
                    layoutNoResults.visibility = View.GONE
                    popularHashtagsLayout.visibility = View.VISIBLE
                }
                isEmpty -> {
                    // Has query but no results
                    rvSearchResults.visibility = View.GONE
                    layoutEmptySearch.visibility = View.GONE
                    layoutNoResults.visibility = View.VISIBLE
                    popularHashtagsLayout.visibility = View.GONE
                }
                else -> {
                    // Has results
                    rvSearchResults.visibility = View.VISIBLE
                    layoutEmptySearch.visibility = View.GONE
                    layoutNoResults.visibility = View.GONE
                    popularHashtagsLayout.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class SearchType {
        ALL, HASHTAG
    }
}