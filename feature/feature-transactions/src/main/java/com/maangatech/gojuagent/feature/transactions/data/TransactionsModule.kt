package com.maangatech.gojuagent.feature.transactions.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionsModule {
    @Binds
    @Singleton
    abstract fun bindWorkflowRepository(impl: DefaultWorkflowRepository): WorkflowRepository
}
