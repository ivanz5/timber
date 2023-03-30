package com.ivanzhur.timbertest.data.repository.contract

import com.ivanzhur.timbertest.core.model.RecordModel
import kotlinx.coroutines.flow.Flow

interface StorageRepository {

    suspend fun getRecordsList(): Flow<List<RecordModel>>

    suspend fun saveRecord(record: RecordModel)

    fun getRecord(id: Int): RecordModel
}