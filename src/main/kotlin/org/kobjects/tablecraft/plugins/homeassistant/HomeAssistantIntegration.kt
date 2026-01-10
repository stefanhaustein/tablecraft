package org.kobjects.tablecraft.plugins.homeassistant

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.pluginapi.AbstractArtifactSpec
import org.kobjects.tablecraft.pluginapi.FunctionInstance
import org.kobjects.tablecraft.pluginapi.FunctionSpec
import org.kobjects.tablecraft.pluginapi.IntegrationInstance
import org.kobjects.tablecraft.pluginapi.IntegrationSpec
import org.kobjects.tablecraft.pluginapi.ModelInterface
import org.kobjects.tablecraft.pluginapi.ParameterSpec
import org.kobjects.tablecraft.pluginapi.Type
import org.kobjects.tablecraft.plugins.homeassistant.client.HAEntity
import org.kobjects.tablecraft.plugins.homeassistant.client.HAEntity.Kind
import org.kobjects.tablecraft.plugins.homeassistant.client.HomeAssistantClient

class HomeAssistantIntegration(
    val model: ModelInterface,
    kind: String,
    name: String,
    tag: Long,
    var host: String,
    var port: Int,
    var token: String,
) : IntegrationInstance(kind, name, tag) {
    var client: HomeAssistantClient? = null

    init {
        attach()
    }

    private fun attach() {
        client = HomeAssistantClient(host, port, token)
    }

    override val operationSpecs: List<AbstractArtifactSpec>
        get() = client?.entities?.values?.filter { it.disabledBy == null }?.map { entityOperationSpec(it) } ?: emptyList()

    override val configuration: Map<String, Any?>
        get() = mapOf("host" to host, "port" to port, "token" to token)

    override fun detach() {
        client?.close()
    }

    override fun reconfigure(configuration: Map<String, Any?>) {
        detach()
        this.host = configuration["host"] as String
        this.port = configuration["port"].toString().toDouble().toInt()
        this.token = configuration["token"] as String
    }

    companion object {

        fun spec(model: ModelInterface) = IntegrationSpec(
            category = "HomeAutomation",
            name = "HomeAssistant",
            "HomeAssistant integration",
            parameters = listOf(
                ParameterSpec(name = "host", type = Type.STRING, defaultValue = "homeassistant.local"),
                ParameterSpec(name = "port", type = Type.INT, defaultValue = 8123),
                ParameterSpec(name = "token", type = Type.STRING, defaultValue = null),
            ),
            modifiers = emptySet(),
        ) { kind, name, tag, config ->
            HomeAssistantIntegration(
                model,
                kind,
                name,
                tag,
                host = config["host"] as String,
                port = config["port"].toString().toDouble().toInt(),
                token = config["token"] as String
            )
        }
    }

    fun entityOperationSpec(entity: HAEntity): FunctionSpec {
        val device = entity.device
        val category = buildString {
            val areaName = device?.area?.toString() ?: "Unnamed Area"
            append(areaName)
            append(".")
            val deviceName = device?.name ?: "Unnamed Device"
            if (deviceName.startsWith(areaName)) {
                append(deviceName.substring(areaName.length).trim())
            } else {
                append(deviceName)
            }
            if (entity.category != null) {
                append("." + entity.category)
            }
        }

        val entityId = entity.id
        val cut = entity.id.indexOf('.')
        val idPrefix = device?.commonEntityIdPrefix ?: ""
        val idWithoutType = entityId.substring(cut + 1)
        val displayName = if (idPrefix.isEmpty() || !idWithoutType.startsWith(idPrefix)) {
            idWithoutType
        } else if (idWithoutType == idPrefix) {
            entityId.take(cut)
        } else {
            val suffix = idWithoutType.substring(idPrefix.length)
            if (suffix.startsWith("_")) suffix.substring(1) else suffix
        }


        return FunctionSpec(
            category = category,
            name = name + "." + entity.id.replace(".", "_"),
            returnType = when (entity.kind) {
                Kind.BINARY_SENSOR -> Type.BOOL
                Kind.LIGHT -> Type.BOOL
                Kind.SENSOR -> Type.REAL
                else -> Type.STRING
            },
            description = entity.description,
            parameters = listOf(),
            displayName = displayName,
        ) {
            EntityFunctionInstance(this@HomeAssistantIntegration, entity.id)
        }
    }

    class EntityFunctionInstance(val integration: HomeAssistantIntegration, val id: String) : FunctionInstance {
        override fun apply(
            context: EvaluationContext,
            params: Map<String, Any?>
        ): Any? {
            return integration.client?.entityStates[id]?.state
        }

    }

}