package com.maangatech.gojuagent.core.common

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Single shared [Moshi] instance for the whole app. Deliberately lives here (not in
 * core-network or core-ussd, both of which need it) so there is exactly one unqualified
 * `Moshi` binding in the Hilt graph — providing it from two modules would be a duplicate
 * binding at compile time once both are on the same component.
 */
@Module
@InstallIn(SingletonComponent::class)
object MoshiModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
}
