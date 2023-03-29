package com.ivanzhur.timbertest.fragment.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivanzhur.timbertest.core.fragment.BaseFragmentWithViewModel
import com.ivanzhur.timbertest.databinding.FragmentListBinding
import com.ivanzhur.timbertest.adapter.RecordListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordListFragment : BaseFragmentWithViewModel<FragmentListBinding, RecordListViewModel>() {

    private val adapter = RecordListAdapter { item ->

    }

    override fun onViewModelCreated() {
        ui.recyclerView.layoutManager = LinearLayoutManager(context)
        ui.recyclerView.adapter = adapter

        ui.button.setOnClickListener { viewModel.testNewItem() }
    }

    override fun observeLiveData() {
        viewModel.recordsLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun getViewModelClass() = RecordListViewModel::class.java

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentListBinding.inflate(inflater, container, false)
}