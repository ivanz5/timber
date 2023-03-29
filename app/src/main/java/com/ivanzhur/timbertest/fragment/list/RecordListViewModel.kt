package com.ivanzhur.timbertest.fragment.list

import androidx.lifecycle.MutableLiveData
import com.ivanzhur.timbertest.core.viewmodel.BaseViewModel
import com.ivanzhur.timbertest.data.model.RecordModel
import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RecordListViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : BaseViewModel() {

    val recordsLiveData = MutableLiveData<List<RecordModel>>()

    fun loadList() = launch(Dispatchers.IO) {
        val list = storageRepository.getRecordsList()
        recordsLiveData.postValue(list)
    }
}