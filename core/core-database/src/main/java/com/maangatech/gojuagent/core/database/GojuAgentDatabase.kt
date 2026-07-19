package com.maangatech.gojuagent.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.maangatech.gojuagent.core.database.dao.CustomerDao
import com.maangatech.gojuagent.core.database.dao.SyncQueueDao
import com.maangatech.gojuagent.core.database.dao.TransactionDao
import com.maangatech.gojuagent.core.database.dao.WorkflowDao
import com.maangatech.gojuagent.core.database.entity.CustomerEntity
import com.maangatech.gojuagent.core.database.entity.SyncQueueEntity
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.core.database.entity.WorkflowDefinitionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CustomerEntity::class,
        WorkflowDefinitionEntity::class,
        SyncQueueEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GojuAgentDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun customerDao(): CustomerDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        const val DATABASE_NAME = "goju_agent.db"
    }
}
