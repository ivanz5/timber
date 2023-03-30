package com.ivanzhur.timbertest.fragment.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.ivanzhur.timbertest.R
import com.ivanzhur.timbertest.core.fragment.BaseFragmentWithViewModel
import com.ivanzhur.timbertest.databinding.FragmentDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordDetailsFragment : BaseFragmentWithViewModel<FragmentDetailsBinding, RecordDetailsViewModel>() {

    private val args: RecordDetailsFragmentArgs by navArgs()

    override fun onViewModelCreated() {
        viewModel.loadData(args.recordId, requireContext())
    }

    override fun observeLiveData() {
        viewModel.bitmapLiveData.observe(viewLifecycleOwner) { bitmap ->
            ui.image.setImageBitmap(bitmap)
        }

        viewModel.recordLiveData.observe(viewLifecycleOwner) { record ->
            ui.results.text = getString(
                R.string.list_item_description,
                record.id, record.lengthValue, record.diameterValue,
            )
        }
    }

    override fun getViewModelClass() = RecordDetailsViewModel::class.java

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDetailsBinding.inflate(inflater, container, false)
}