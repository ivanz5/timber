package com.ivanzhur.timbertest.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB: ViewBinding> : Fragment() {

    private var _viewBinding: VB? = null
    // Only valid between onCreateView and onDestroyView
    protected val ui: VB get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = setupViewBinding(inflater, container).let {
        _viewBinding = it
        it.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    protected abstract fun setupViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    // TODO: 2/23/21  onActivityCreated deprecated
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        onReady()
    }

    protected open fun handleError(throwable: Throwable) {
        showToast(throwable.localizedMessage ?: throwable.message ?: "Unexpected exception")
    }

    fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
        context?.let { Toast.makeText(it, message, length).show() }
    }

    /**
     * Initialize UI here. DO NOT override it if using BaseFragmentWithViewModel,
     * because it overrides this method as well and ViewModel will not be initialized correctly.
     * @see BaseFragmentWithViewModel
     */
    protected abstract fun onReady()
}