package com.maangatech.gojuagent.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maangatech.gojuagent.core.database.entity.WorkflowDefinitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workflow: WorkflowDefinitionEntity)

    @Query("SELECT * FROM workflow_definitions WHERE providerCode = :providerCode AND serviceType = :serviceType AND isActive = 1 LIMIT 1")
    suspend fun findActive(providerCode: String, serviceType: String): WorkflowDefinitionEntity?

    @Query("SELECT * FROM workflow_definitions WHERE isActive = 1 ORDER BY providerName ASC")
    fun observeActive(): Flow<List<WorkflowDefinitionEntity>>

    @Query("SELECT DISTINCT providerCode, providerName FROM workflow_definitions WHERE isActive = 1")
    fun observeActiveProviders(): Flow<List<ProviderSummary>>
}

data class ProviderSummary(val providerCode: String, val providerName: String)
