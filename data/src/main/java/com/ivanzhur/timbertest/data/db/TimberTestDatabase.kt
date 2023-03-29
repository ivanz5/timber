package com.ivanzhur.timbertest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ivanzhur.timbertest.data.db.dao.RecordsDao
import com.ivanzhur.timbertest.data.model.RecordEntity

@Database(version = 1, entities = [
    RecordEntity::class
])
abstract class TimberTestDatabase : RoomDatabase() {

    abstract fun recordsDao(): RecordsDao

    companion object {
        const val DATABASE_NAME = "timber_test_database.db"
    }
}