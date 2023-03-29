package com.ivanzhur.timbertest.data.repository.impl

import com.ivanzhur.timbertest.data.model.RecordModel
import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor() : StorageRepository {

    override fun getRecordsList(): List<RecordModel> {
        return listOf(
            RecordModel(1), RecordModel(2), RecordModel(3),
        )
    }
}