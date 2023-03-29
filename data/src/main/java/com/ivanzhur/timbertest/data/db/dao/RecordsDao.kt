package com.ivanzhur.timbertest.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ivanzhur.timbertest.data.model.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordsDao {

    @Query("""SELECT * FROM records ORDER BY id DESC""")
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Insert
    fun saveRecord(record: RecordEntity)
}