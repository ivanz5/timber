package com.ivanzhur.timbertest.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class BaseViewModel : ViewModel() {

    /**
     * Launch coroutine on viewModelScope.
     * If context doesn't have CoroutineExceptionHandler - use default handler
     * (log error and NOT rethrow).
     */
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val mergedContext =
            if (context is CoroutineExceptionHandler || context[CoroutineExceptionHandler] != null) {
                context
            }
            else {
                context + CoroutineExceptionHandler { _, throwable ->
                    Timber.e(throwable)
                }
            }
        viewModelScope.launch(mergedContext, start, block)
    }
}