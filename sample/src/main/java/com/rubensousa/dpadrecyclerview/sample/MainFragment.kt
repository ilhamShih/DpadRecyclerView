package com.rubensousa.dpadrecyclerview.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ViewHolderTask
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenTvNestedListsBinding
import com.rubensousa.dpadrecyclerview.sample.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.list.DpadStateHolder
import com.rubensousa.dpadrecyclerview.sample.list.ListPlaceholderAdapter
import com.rubensousa.dpadrecyclerview.sample.list.NestedListAdapter
import timber.log.Timber

class MainFragment : Fragment(R.layout.screen_tv_nested_lists) {

    private var _binding: ScreenTvNestedListsBinding? = null
    private val binding: ScreenTvNestedListsBinding get() = _binding!!
    private var selectedPosition = RecyclerView.NO_POSITION
    private val scrollStateHolder = DpadStateHolder()
    private val viewModel by viewModels<MainViewModel>()
    private val loadingAdapter = ListPlaceholderAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ScreenTvNestedListsBinding.bind(view)
        val nestedListAdapter = setupAdapter()
        setupAlignment(binding.recyclerView)
        setupPagination(binding.recyclerView)
        viewModel.listState.observe(viewLifecycleOwner) { list ->
            nestedListAdapter.submitList(list) {
                binding.recyclerView.invalidateItemDecorations()
            }
        }
        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            loadingAdapter.show(isLoading)
        }
        val itemAnimator = binding.recyclerView.itemAnimator as DefaultItemAnimator
        itemAnimator.removeDuration = 500

        binding.recyclerView.requestFocus()
        if (selectedPosition != RecyclerView.NO_POSITION) {
            binding.recyclerView.setSelectedPosition(
                selectedPosition,
                object : ViewHolderTask() {
                    override fun execute(viewHolder: RecyclerView.ViewHolder) {
                        Timber.d("Selection state restored")
                    }
                })
        }
    }

    private fun setupAdapter(): NestedListAdapter {
        val concatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(true)
                .build()
        )
        val nestedListAdapter = NestedListAdapter(scrollStateHolder,
            object : ItemViewHolder.ItemClickListener {
                override fun onViewHolderClicked() {
                    findNavController().navigate(R.id.open_detail)
                }
            })
        concatAdapter.addAdapter(nestedListAdapter)
        concatAdapter.addAdapter(loadingAdapter)
        nestedListAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerView.adapter = concatAdapter
        return nestedListAdapter
    }

    private fun setupPagination(recyclerView: DpadRecyclerView) {
        recyclerView.addOnViewHolderSelectedListener(object :
            OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                selectedPosition = position
                viewModel.loadMore(selectedPosition)
                Timber.d("Selected: $position, $subPosition")
            }

            override fun onViewHolderSelectedAndAligned(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                Timber.d("Aligned: $position, $subPosition")
            }
        })
    }

    private fun setupAlignment(recyclerView: DpadRecyclerView) {
        recyclerView.addItemDecoration(
            LinearMarginDecoration.createVertical(
                verticalMargin = resources.getDimensionPixelOffset(R.dimen.item_spacing)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}