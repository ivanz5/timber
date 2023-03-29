package com.ivanzhur.timbertest.data.di

import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import com.ivanzhur.timbertest.data.repository.impl.StorageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindStorageRepository(storageRepositoryImpl: StorageRepositoryImpl): StorageRepository
}