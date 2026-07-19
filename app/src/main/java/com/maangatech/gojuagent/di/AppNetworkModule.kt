package com.maangatech.gojuagent.di

import com.maangatech.gojuagent.BuildConfig
import com.maangatech.gojuagent.core.network.di.BaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {
    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = BuildConfig.DEFAULT_SERVER_URL
}
