package org.kobjects.pi123.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.pi123.pluginapi.*

class Port(
    val name: String,
    val portConstructor: OperationSpec,
    val configuration: Map<String, Any>
) {
    val specification = OperationSpec(OperationKind.PORT_INSTANCE,
        portConstructor.returnType,
        name,
        "${portConstructor.name} instance",
        portConstructor.parameters.filter { it.kind != ParameterKind.CONFIGURATION },
        ::createInstance)


    fun createInstance(host: OperationHost): OperationInstance {
        throw UnsupportedOperationException()
    }


    fun toJson(): String {
        val configJson = configuration.entries.joinToString {  "${it.key.quote()}: ${it.value.toString().quote()}" }

        return """{"name":${name.quote()}, "type":${portConstructor.name.quote()}, "configuration": {$configJson} } """
    }

}