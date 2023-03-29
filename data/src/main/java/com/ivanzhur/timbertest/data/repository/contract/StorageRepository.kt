package com.ivanzhur.timbertest.data.repository.contract

import com.ivanzhur.timbertest.data.model.RecordModel

interface StorageRepository {

    fun getRecordsList(): List<RecordModel>
}