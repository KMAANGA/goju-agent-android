package com.maangatech.gojuagent.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A cached, versioned USSD workflow definition fetched from GOJU Cloud. [definitionJson] is
 * opaque here on purpose — core-database has no business parsing USSD step logic, that's
 * core-ussd's job. Keeping the last-known-good row per (providerCode, serviceType) is what
 * lets the automation engine keep working if a sync fetch fails or returns something the
 * engine can't parse.
 */
@Entity(
    tableName = "workflow_definitions",
    indices = [
        Index(value = ["providerCode", "serviceType"], unique = true),
    ],
)
data class WorkflowDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerCode: String,
    val providerName: String,
    val serviceType: String,
    val version: Int,
    val definitionJson: String,
    val isActive: Boolean = true,
    val updatedAt: Long,
)
