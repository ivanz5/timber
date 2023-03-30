package com.ivanzhur.timbertest.data.di

import android.content.Context
import androidx.room.Room
import com.ivanzhur.timbertest.data.db.TimberTestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): TimberTestDatabase {
        return Room.databaseBuilder(
            context,
            TimberTestDatabase::class.java,
            TimberTestDatabase.DATABASE_NAME,
        ).fallbackToDestructiveMigration().build()
    }
}