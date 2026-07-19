package com.maangatech.gojuagent.core.database.di

import com.maangatech.gojuagent.core.database.repository.CustomerRepository
import com.maangatech.gojuagent.core.database.repository.DefaultCustomerRepository
import com.maangatech.gojuagent.core.database.repository.DefaultTransactionRepository
import com.maangatech.gojuagent.core.database.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: DefaultTransactionRepository): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(impl: DefaultCustomerRepository): CustomerRepository
}
