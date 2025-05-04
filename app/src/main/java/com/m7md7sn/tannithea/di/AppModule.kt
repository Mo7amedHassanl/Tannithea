package com.m7md7sn.tannithea.di

import com.m7md7sn.tannithea.data.repository.FirebaseSensorRepository
import com.m7md7sn.tannithea.data.repository.SensorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseSensorRepository(): FirebaseSensorRepository = FirebaseSensorRepository()
    
    @Provides
    @Singleton
    fun provideSensorRepository(firebaseRepository: FirebaseSensorRepository): SensorRepository = 
        SensorRepository(firebaseRepository)
} 