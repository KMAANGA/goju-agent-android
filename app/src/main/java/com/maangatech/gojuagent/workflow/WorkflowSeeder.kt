package com.maangatech.gojuagent.workflow

import android.content.Context
import com.maangatech.gojuagent.core.database.dao.WorkflowDao
import com.maangatech.gojuagent.core.database.entity.WorkflowDefinitionEntity
import com.maangatech.gojuagent.core.ussd.WorkflowJsonParser
import com.maangatech.gojuagent.core.ussd.model.WorkflowDefinition
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads `assets/workflows/mpesa_wakala_v1.json` into Room on first launch — see that file's
 * companion README for why its contents are a template, not a verified live menu dump. Only
 * seeds when the workflow table is empty, so it never overwrites anything a real sync with
 * GOJU Cloud has already fetched.
 */
@Singleton
class WorkflowSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workflowDao: WorkflowDao,
    private val jsonParser: WorkflowJsonParser,
    private val moshi: Moshi,
) {
    suspend fun seedIfEmpty() {
        val alreadySeeded = workflowDao.findActive("MPESA", "withdraw") != null
        if (alreadySeeded) return

        val json = context.assets.open(SEED_ASSET_PATH).bufferedReader().use { it.readText() }
        val listType = Types.newParameterizedType(List::class.java, WorkflowDefinition::class.java)
        val definitions = moshi.adapter<List<WorkflowDefinition>>(listType).fromJson(json) ?: return

        definitions.forEach { definition ->
            workflowDao.upsert(
                WorkflowDefinitionEntity(
                    providerCode = definition.providerCode,
                    providerName = definition.providerName,
                    serviceType = definition.serviceType,
                    version = definition.version,
                    definitionJson = jsonParser.serialize(definition),
                    isActive = true,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    private companion object {
        const val SEED_ASSET_PATH = "workflows/mpesa_wakala_v1.json"
    }
}
