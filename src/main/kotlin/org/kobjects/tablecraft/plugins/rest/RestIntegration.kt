package org.kobjects.tablecraft.plugins.rest


import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class RestIntegration(
    val configuration: Map<String, Any>
) : Integration {
    val name = configuration["name"] as String
    override val tag = (configuration["tag"] as Number).toLong()

    val values = mutableMapOf<String, Any>()
    init {
        // embeddedServer(CIO, port = configuration["port"]?.toString()?.toInt() ?: 8088) {}
    }


    override fun detach() {

    }

    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":"Rest", "configuration": """)
        configuration.filterKeys { it != "name" && it != "tag" }.toJson(sb)
        sb.append("}")
    }

    override val operationSpecs: List<OperationSpec> = listOf(
        OperationSpec(
            OperationKind.OUTPUT_PORT,
            Type.TEXT,
            name + ".out",
            "Sets the field with the given name in the JSON output of this REST server.",
            listOf(
                ParameterSpec("fieldName", Type.TEXT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("value",  Type.TEXT)),
            tag
        ) {
            Out(it["fieldName"] as String)
        }
    )


    inner class Out(val name: String) : Operation {

        override fun apply(params: Map<String, Any>): Any {
            val value = params["value"]!!
            values[name] = value
            println("Added $value to $values")
            return value
        }

    }
}