package com.ivanzhur.timbertest.fragment.list

import androidx.lifecycle.MutableLiveData
import com.ivanzhur.timbertest.core.viewmodel.BaseViewModel
import com.ivanzhur.timbertest.core.model.RecordModel
import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RecordListViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : BaseViewModel() {

    val recordsLiveData = MutableLiveData<List<RecordModel>>()

    init {
        subscribeToListUpdates()
    }

    private fun subscribeToListUpdates() = launch(Dispatchers.IO) {
        storageRepository.getRecordsList()
            .catch { t -> Timber.e(t) }
            .collect { recordsLiveData.postValue(it) }
    }
}