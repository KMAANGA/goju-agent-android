package com.maangatech.gojuagent.core.database.di

import android.content.Context
import androidx.room.Room
import com.maangatech.gojuagent.core.database.GojuAgentDatabase
import com.maangatech.gojuagent.core.database.dao.CustomerDao
import com.maangatech.gojuagent.core.database.dao.SyncQueueDao
import com.maangatech.gojuagent.core.database.dao.TransactionDao
import com.maangatech.gojuagent.core.database.dao.WorkflowDao
import com.maangatech.gojuagent.core.security.DatabasePassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        passphraseProvider: DatabasePassphraseProvider,
    ): GojuAgentDatabase {
        // Loads the SQLCipher native library once per process before any DB is opened.
        SQLiteDatabase.loadLibs(context)

        val passphrase = SQLiteDatabase.getBytes(passphraseProvider.getOrCreatePassphrase())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(context, GojuAgentDatabase::class.java, GojuAgentDatabase.DATABASE_NAME)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    @Provides
    fun provideTransactionDao(db: GojuAgentDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCustomerDao(db: GojuAgentDatabase): CustomerDao = db.customerDao()

    @Provides
    fun provideWorkflowDao(db: GojuAgentDatabase): WorkflowDao = db.workflowDao()

    @Provides
    fun provideSyncQueueDao(db: GojuAgentDatabase): SyncQueueDao = db.syncQueueDao()
}
