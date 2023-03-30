package com.ivanzhur.timbertest.fragment.list

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivanzhur.timbertest.core.fragment.BaseFragmentWithViewModel
import com.ivanzhur.timbertest.databinding.FragmentListBinding
import com.ivanzhur.timbertest.adapter.RecordListAdapter
import com.ivanzhur.timbertest.fragment.measure.MeasurementFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordListFragment : BaseFragmentWithViewModel<FragmentListBinding, RecordListViewModel>() {

    /**
     * Image picker intent and result handling.
     * If the image was selected by user - navigate to [MeasurementFragment] passing image Uri.
     */
    private val imagePickLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
//        requireActivity().applicationContext.grantUriPermission(requireActivity().packageName, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        requireActivity().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        uri?.let {
            val action = RecordListFragmentDirections.actionNavigationRecordListToNavigationMeasurement(it)
            findNavController().navigate(action)
        }
    }

    private val adapter = RecordListAdapter { item ->
        val action = RecordListFragmentDirections.actionNavigationRecordListToNavigationDetails(item.id)
        findNavController().navigate(action)
    }

    override fun onViewModelCreated() {
        ui.recyclerView.layoutManager = LinearLayoutManager(context)
        ui.recyclerView.adapter = adapter

        // Launch image picker.
        // If the image was selected - it will be handled by imagePickLauncher
        ui.button.setOnClickListener {
            imagePickLauncher.launch(arrayOf("image/*"))
        }
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