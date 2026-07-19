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
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
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
        System.loadLibrary("sqlcipher")

        // The passphrase is a random hex string (see DatabasePassphraseProvider) — its own
        // UTF-8 bytes are the key material, no separate hex-decode step needed.
        val passphrase = String(passphraseProvider.getOrCreatePassphrase()).toByteArray(Charsets.UTF_8)
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(context, GojuAgentDatabase::class.java, GojuAgentDatabase.DATABASE_NAME)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigrationOnDowngrade()
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
