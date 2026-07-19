package com.maangatech.gojuagent.core.network.di

import com.maangatech.gojuagent.core.network.AuthInterceptor
import com.maangatech.gojuagent.core.network.api.AgentAuthApi
import com.maangatech.gojuagent.core.network.api.AgentTransactionApi
import com.maangatech.gojuagent.core.network.api.AgentWorkflowApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // BODY logging must never ship to release — customer numbers/amounts/PINs would land in logcat.
            level = HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi, @BaseUrl baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideAgentAuthApi(retrofit: Retrofit): AgentAuthApi = retrofit.create(AgentAuthApi::class.java)

    @Provides
    @Singleton
    fun provideAgentWorkflowApi(retrofit: Retrofit): AgentWorkflowApi = retrofit.create(AgentWorkflowApi::class.java)

    @Provides
    @Singleton
    fun provideAgentTransactionApi(retrofit: Retrofit): AgentTransactionApi =
        retrofit.create(AgentTransactionApi::class.java)
}
