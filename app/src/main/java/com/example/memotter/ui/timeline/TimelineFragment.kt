package com.example.memotter.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.data.database.MemotterDatabase
import com.example.memotter.data.repository.MemoRepository
import com.example.memotter.databinding.FragmentTimelineBinding

class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    private lateinit var timelineViewModel: TimelineViewModel
    private lateinit var memoAdapter: MemoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupSwipeRefresh()
        observeData()
    }

    private fun setupViewModel() {
        val database = MemotterDatabase.getDatabase(requireContext())
        val repository = MemoRepository(database.memoDao(), database.hashtagDao(), requireContext())
        val factory = TimelineViewModelFactory(repository)
        timelineViewModel = ViewModelProvider(this, factory)[TimelineViewModel::class.java]
    }

    private fun setupRecyclerView() {
        memoAdapter = MemoAdapter(
            onMemoClick = { memo ->
                // TODO: Navigate to memo detail/edit
            },
            onFavoriteClick = { memo ->
                timelineViewModel.toggleFavorite(memo.id, !memo.isFavorite)
            }
        )

        binding.rvTimeline.apply {
            adapter = memoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            timelineViewModel.refreshMemos()
        }
    }

    private fun observeData() {
        timelineViewModel.memos.observe(viewLifecycleOwner) { memos ->
            memoAdapter.submitList(memos)
            updateEmptyState(memos.isEmpty())
            binding.swipeRefresh.isRefreshing = false
        }

        timelineViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTimeline.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}