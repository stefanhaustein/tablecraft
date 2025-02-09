package org.kobjects.tablecraft.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kobjects.tablecraft.model.builtin.BuiltinFunctions
import org.kobjects.tablecraft.pluginapi.OperationSpec
import org.kobjects.tablecraft.pluginapi.ParameterKind
import org.kobjects.tablecraft.pluginapi.Plugin
import org.kobjects.tablecraft.plugins.pi4j.Pi4jPlugin
import org.kobjects.tablecraft.svg.SvgManager
import java.io.File
import java.io.FileWriter
import org.kobjects.tablecraft.toml.IniParser
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object Model {
    var modificationTag: Long = 0
    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))
    val lock = ReentrantLock()
    val listeners = mutableSetOf<() -> Unit>()

    val functionMap = mutableMapOf<String, OperationSpec>()
    val plugins = mutableListOf<Plugin>()
    val ports = mutableMapOf<String, Port>()

    val svgs = SvgManager(File("src/main/resources/static/img"))

    fun addPlugin(plugin: Plugin) {
        plugins.add(plugin)
        for (function in plugin.operationSpecs) {
            functionMap[function.name] = function
        }
    }

    init {
        addPlugin(BuiltinFunctions)
        addPlugin(Pi4jPlugin())
        addPlugin(svgs)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <T> withLock(action: (RuntimeContext) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        return lock.withLock {
            val runtimeContext = RuntimeContext()
            action(runtimeContext)
        }
    }

    init {
        withLock { runtimeContext ->
            try {
                val toml = IniParser.parse(File("storage/model.ini").readText())
                for ((key, map) in toml) {
                    if (key.startsWith("sheets.") && key.endsWith(".cells")) {
                        val name = key.substringAfter("sheets.").substringBeforeLast(".cells")
                        val sheet = Sheet(name)
                        sheets[name] = sheet
                        sheet.parseToml(map)
                    } else if (key == "ports") {
                        for ((name, value) in map) {
                            definePort(name, Json.parseToJsonElement(value.toString()) as JsonObject)
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            for (sheet in sheets.values) {
                sheet.updateAll(runtimeContext)
            }
        }
    }


    fun set(name: String, value: String, runtimeContext: RuntimeContext?) {
        val cut = name.indexOf("!")
        val sheet = sheets[name.substring(0, cut)]!!
        sheet.set(name.substring(cut + 1), value, runtimeContext)
    }

    fun save() {
        File("storage").mkdir()
        val writer = FileWriter("storage/model.ini")
        writer.write("[ports]\n\n")

        for (port in ports.values) {
            writer.write("${port.name} = ${port.toJson().quote()}\n")
        }
        writer.write("\n")

        for (sheet in sheets.values) {
            writer.write(sheet.serialize(-1, false))
            writer.write("\n")
        }
        writer.close()
    }


    fun notifyContentUpdated(runtimeContext: RuntimeContext) {
        modificationTag = runtimeContext.tag
        for (listener in listeners) {listener()}
        listeners.clear()
    }

    fun functionsToJson() = functionMap.values.joinToString(",\n", "[\n", "\n]\n") { it.toJson() }

    fun definePort(name: String, jsonSpec: JsonObject) {
        val type = jsonSpec["type"]!!.jsonPrimitive.content
        val constructorSpecification = functionMap[type]!!

        val configuration = mutableMapOf<String, Any>()
        val jsonConfig = jsonSpec["configuration"]!!.jsonObject

        for (paramSpec in constructorSpecification.parameters) {
            if (paramSpec.kind == ParameterKind.CONFIGURATION) {
                configuration[paramSpec.name] = paramSpec.type.fromString(jsonConfig[paramSpec.name]!!.jsonPrimitive.content)
            }
        }

        val port = Port(name, constructorSpecification, configuration.toMap())
        ports[name] = port
        functionMap[name] = port.specification

        save()
    }
}