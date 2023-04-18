package com.example.testble.di

import com.example.testble.data.BleConnectionController
import com.example.testble.data.BleConnectionControllerImpl
import com.example.testble.data.BleScanController
import com.example.testble.data.BleScanControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {
    @Binds
    @ViewModelScoped
    abstract fun bindBleScanControllerImpl(
        bleScanControllerImpl: BleScanControllerImpl
    ): BleScanController

    @Binds
    @ViewModelScoped
    abstract fun bindBleConnectionControllerImpl(
        bleConnectionControllerImpl: BleConnectionControllerImpl
    ): BleConnectionController
}