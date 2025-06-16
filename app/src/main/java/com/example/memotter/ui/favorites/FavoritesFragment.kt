package com.example.memotter.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memotter.data.database.MemotterDatabase
import com.example.memotter.data.repository.MemoRepository
import com.example.memotter.databinding.FragmentFavoritesBinding
import com.example.memotter.ui.timeline.MemoAdapter

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var memoAdapter: MemoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
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
        val factory = FavoritesViewModelFactory(repository)
        favoritesViewModel = ViewModelProvider(this, factory)[FavoritesViewModel::class.java]
    }

    private fun setupRecyclerView() {
        memoAdapter = MemoAdapter(
            onMemoClick = { memo ->
                // TODO: Navigate to memo detail/edit
            },
            onFavoriteClick = { memo ->
                favoritesViewModel.toggleFavorite(memo.id, !memo.isFavorite)
            }
        )

        binding.rvFavorites.apply {
            adapter = memoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            favoritesViewModel.refreshFavorites()
        }
    }

    private fun observeData() {
        favoritesViewModel.favoriteMemos.observe(viewLifecycleOwner) { memos ->
            memoAdapter.submitList(memos)
            updateEmptyState(memos.isEmpty())
            binding.swipeRefresh.isRefreshing = false
        }

        favoritesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvFavorites.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}