package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.model.Model.integrations
import org.kobjects.tablecraft.pluginapi.IntegrationInstance
import org.kobjects.tablecraft.pluginapi.IntegrationSpec
import org.kobjects.tablecraft.pluginapi.ModificationToken
import java.io.Writer

class Integrations : Iterable<IntegrationInstance> {

    val integrationMap = mutableMapOf<String, IntegrationInstance>()

    fun deleteIntegration(name: String, token: ModificationToken) {
        val integration = integrationMap[name]
        if (integration != null) {
            integration.detach()
            integrationMap[name] = IntegrationInstance.Tombstone(integration, token.tag)
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
            integrationMap[name]?.detach()

            val type = jsonSpec["type"].toString()
            val specification = Model.functionMap[type] as IntegrationSpec

            val config = specification.convertConfiguration(jsonSpec["configuration"] as Map<String, Any>) +
                    mapOf("name" to name, "tag" to token.tag)

            val integration = specification.createFn(config)
            integrationMap[name] = integration

            for (operation in integration.operationSpecs) {
                Model.functionMap[operation.name] = operation
            }

            token.symbolsChanged = true
        }
    }


    fun serialize(writer: Writer, forClient: Boolean, tag: Long) {
        val sb = StringBuilder()
        for (integration in integrations) {
            if (integration.tag > tag && (forClient || integration !is IntegrationInstance.Tombstone)) {
                sb.append(integration.name).append(": ")
                integration.toJson(sb)
                sb.append('\n')
            }
        }
        if (sb.isNotEmpty()) {
            writer.write("[integrations]\n\n")
            writer.write(sb.toString())
        }
    }


    override fun iterator() = integrationMap.values.iterator()
}