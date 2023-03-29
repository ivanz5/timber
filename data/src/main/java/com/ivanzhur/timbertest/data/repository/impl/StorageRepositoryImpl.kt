package com.ivanzhur.timbertest.data.repository.impl

import com.ivanzhur.timbertest.core.model.RecordModel
import com.ivanzhur.timbertest.data.db.TimberTestDatabase
import com.ivanzhur.timbertest.data.mapper.mapToEntity
import com.ivanzhur.timbertest.data.mapper.mapToModel
import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val db: TimberTestDatabase
) : StorageRepository {

    override suspend fun getRecordsList(): Flow<List<RecordModel>> {
        return db.recordsDao().getAllRecords().map { list ->
            list.map { it.mapToModel() }
        }
    }

    override suspend fun saveRecord(record: RecordModel) {
        db.recordsDao().saveRecord(record.mapToEntity())
    }
}