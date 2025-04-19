package org.kobjects.tablecraft.plugins.pi4j


import freenove.Freenove_LCD1602
import org.kobjects.tablecraft.json.quote
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.pluginapi.*

class Lcd1602(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>
)  : Integration {

    val name = configuration["name"] as String

    override val tag = configuration["tag"] as Long
    override val type: String
        get() = "Lcd1602"

    var display: Freenove_LCD1602?
    var error: Exception?

    init {
        try {
            display = Freenove_LCD1602()
            error = null
        } catch (e: Exception) {
            e.printStackTrace()
            error = e
            display = null
        }
    }


    override fun toJson(sb: StringBuilder) {
        sb.append("""{"name":${name.quote()}, "type":"Lcd1602", "configuration": """)
        configuration.filterKeys { it != "name" && it != "tag" }.toJson(sb)
        sb.append("}")
    }

    override fun detach() {
    }

    override val operationSpecs: List<OperationSpec> = listOf(
        OperationSpec(
            OperationKind.OUTPUT_PORT,
            Type.TEXT,
            name + ".section",
            ".",
            listOf(
                ParameterSpec("x", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("y", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT)),
                ParameterSpec("width", Type.INT, setOf(ParameterSpec.Modifier.CONSTANT, ParameterSpec.Modifier.OPTIONAL)),
            ),
            emptySet(),
            tag
        ) {
            Section(it["x"] as Int, it["y"] as Int)
        })


    inner class Section(val x: Int, val y: Int) : Operation {

        override fun apply(params: Map<String, Any>): Any {
            val text = params["value"].toString()
            if (error != null) {
                return error!!
            }
            display?.position(x, y)
            display?.puts(text)
            return text
        }
    }

}