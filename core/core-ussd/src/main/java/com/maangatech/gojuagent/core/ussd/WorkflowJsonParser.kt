package com.maangatech.gojuagent.core.ussd

import com.maangatech.gojuagent.core.ussd.model.WorkflowDefinition
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses the opaque `definitionJson` blob cached in Room (see `WorkflowDefinitionEntity`)
 * back into a [WorkflowDefinition] the engine can run. Isolated behind this class so a
 * malformed/unparseable server payload degrades to "no workflow available" rather than a
 * crash — callers should fall back to the last version that parsed successfully.
 */
@Singleton
class WorkflowJsonParser @Inject constructor(private val moshi: Moshi) {

    private val adapter = moshi.adapter(WorkflowDefinition::class.java)

    fun parse(json: String): WorkflowDefinition? = runCatching { adapter.fromJson(json) }.getOrNull()

    fun serialize(definition: WorkflowDefinition): String = adapter.toJson(definition)
}
