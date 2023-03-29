package com.ivanzhur.timbertest.core.fragment

import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.ivanzhur.timbertest.core.viewmodel.BaseViewModel

abstract class BaseFragmentWithViewModel<VB: ViewBinding, VM : BaseViewModel> : BaseFragment<VB>() {

    lateinit var viewModel: VM

    override fun onReady() {
        viewModel = getViewModel(getViewModelClass())

        onViewModelCreated()
        observeLiveData()
    }

    /**
     * Doesn't reuse ViewModels. Each time new instance is created.
     */
    private fun <T : BaseViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProvider(viewModelStore, defaultViewModelProviderFactory).get(clazz)
    }

    protected abstract fun onViewModelCreated()

    protected abstract fun observeLiveData()

    protected abstract fun getViewModelClass(): Class<VM>
}