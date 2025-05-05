package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.pluginapi.IntegrationInstance
import org.kobjects.tablecraft.pluginapi.IntegrationSpec
import org.kobjects.tablecraft.pluginapi.ModificationToken

object Integrations {

    fun deleteIntegration(name: String, token: ModificationToken) {
        val integration = Model.integrationMap[name]
        if (integration != null) {
            integration.detach()
            Model.integrationMap[name] = IntegrationInstance.Tombstone(integration, token.tag)
        }
        token.symbolsChanged = true
    }

    fun defineIntegration(name: String?,  jsonSpec: Map<String, Any>, token: ModificationToken) {
        val previousName = jsonSpec["previousName"] as? String?

        if (!previousName.isNullOrBlank()) {
            try {
                deleteIntegration(previousName, token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!name.isNullOrBlank()) {
            Model.integrationMap[name]?.detach()

            val type = jsonSpec["type"].toString()
            val specification = Model.functionMap[type] as IntegrationSpec

            val config = specification.convertConfiguration(jsonSpec["configuration"] as Map<String, Any>) +
                    mapOf("name" to name, "tag" to token.tag)

            val integration = specification.createFn(config)
            Model.integrationMap[name] = integration

            for (operation in integration.operationSpecs) {
                Model.functionMap[operation.name] = operation
            }

            token.symbolsChanged = true
        }
    }
}