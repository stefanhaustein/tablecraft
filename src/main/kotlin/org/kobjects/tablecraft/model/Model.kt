package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.builtin.BuiltinFunctions
import org.kobjects.tablecraft.pluginapi.*
import org.kobjects.tablecraft.plugins.pi4j.Pi4jPlugin
import org.kobjects.tablecraft.svg.SvgManager
import java.io.File
import java.io.FileWriter
import org.kobjects.tablecraft.tomson.TomsonParser
import java.io.Writer

object Model {

    val STORAGE_FILE = File("storage/data.tc")

    var modificationTag: Long = 0
    var simulationMode_: Boolean = false

    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))
    val listeners = mutableSetOf<() -> Unit>()

    val functionMap = mutableMapOf<String, OperationSpec>()
    val plugins = mutableListOf<Plugin>()

    val portMap = mutableMapOf<String, Port>()
    val simulationValueMap = mutableMapOf<String, Any>()

    val svgs = SvgManager(File("src/main/resources/static/img"))


    init {
        addPlugin(BuiltinFunctions)
        addPlugin(Pi4jPlugin())
        addPlugin(svgs)
        // addPlugin(MqttPlugin)

        ModificationToken.applySynchronizedWithToken { runtimeContext ->
            loadData(STORAGE_FILE.readText(), runtimeContext)
        }
    }

    fun addPlugin(plugin: Plugin) {
        plugins.add(plugin)
        for (function in plugin.operationSpecs) {
            functionMap[function.name] = function
        }
    }

    fun setSimulationMode(value: Boolean, token: ModificationToken) {
        simulationMode_ = value
        for (port in portMap.values) {
            val simulationValue = simulationValueMap[port.name]
            if (simulationValue != null) {
                port.notifyValueChanged(simulationValue, token)
            }
        }
    }



    fun loadData(data: String, token: ModificationToken) {
        try {
            val toml = TomsonParser.parse(data)
            for ((key, map) in toml) {
                if (key.isEmpty()) {
                    setSimulationMode(map["simulationMode"] as Boolean? ?: false, token)
                } else if (key.startsWith("sheets.") && key.endsWith(".cells")) {
                    val name = key.substringAfter("sheets.").substringBeforeLast(".cells")
                    val sheet = Sheet(name)
                    sheets[name] = sheet
                    sheet.parseToml(map)
                } else if (key == "ports") {
                    for ((name, value) in map) {
                        try {
                            definePort(name, value as Map<String, Any>, token)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        for (sheet in sheets.values) {
            sheet.updateAll(token)
        }
    }

    fun getOrCreate(name: String): Cell {
        val cut = name.indexOf("!")
        val sheet = sheets[name.substring(0, cut)]!!
        return sheet.getOrCreateCell(name.substring(cut + 1))
    }

    fun serialize(writer: Writer, forClient: Boolean = false, tag: Long = -1) {
        writer.write("simulationMode = $simulationMode_\n\n")

        if (forClient) {
            writer.write(serializeFunctions(tag))
        }

        serializePorts(writer, tag)

        writer.write("\n")

        for (sheet in sheets.values) {
            writer.write(sheet.serialize(tag, forClient))
            writer.write("\n")
        }
    }


    fun save(modificationToken: ModificationToken) {
        STORAGE_FILE.mkdirs()
        val writer = FileWriter(STORAGE_FILE)
        serialize(writer)
        writer.close()
    }


    fun notifyContentUpdated(modificationToken: ModificationToken) {
        modificationTag = modificationToken.tag
        for (listener in listeners) {listener()}
        listeners.clear()
    }


    fun serializeFunctions(tag: Long): String {
        val sb = StringBuilder()
        for (function in functionMap.values) {
            if (function.tag > tag) {
                sb.append(function.name).append(": ")
                function.toJson(sb)
                sb.append('\n')
            }
        }
        return if (sb.isEmpty()) "" else "[functions]\n\n$sb"
    }

    fun serializePorts(writer: Writer, tag: Long) {
        val definitions = StringBuilder()
        val values = StringBuilder()
        for (port in portMap.values) {
            if (port.tag > tag) {
                definitions.append(port.name).append(": ")
                port.toJson(definitions)
                definitions.append('\n')
            }
            if (port.expression?.valueTag ?: -1 > tag) {
                values.append("${port.name}: ${port.value.toJson()}\n")
            }
        }

        if (definitions.isNotEmpty()) {
            writer.write("[ports]\n\n$definitions\n")
        }
        if (values.isNotEmpty()) {
            writer.write("[portValues]\n\n$values\n")
        }

        if (simulationValueMap.isNotEmpty()) {
            writer.write("[simulationValues]\n\n")
            for ((key, value) in simulationValueMap) {
                writer.write("$key: ${value.toJson()}\n")
            }
            writer.write("\n")
        }
    }

    fun deletePort(name: String, modificationToken: ModificationToken) {
        portMap[name] = Port(name,  "tombstone", emptyMap(), modificationToken.tag)
    }

    fun definePort(name: String?, jsonSpec: Map<String, Any>, token: ModificationToken) {
        val previousName = jsonSpec["previousName"] as String?

        if (!previousName.isNullOrBlank()) {
            try {
                deletePort(previousName, token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!name.isNullOrBlank()) {
            val type = jsonSpec["type"].toString()
            val config = jsonSpec["configuration"] as Map<String, Any>

            val port = Port(name, type, config, token.tag)
            val expression = jsonSpec["expression"] as String?
            if (expression != null) {
                port.setExpression(expression, token)
            }
            portMap[name] = port
        }
    }

    fun clearAll(modificationToken: ModificationToken) {
        for (key in portMap.keys.toList()) {
            deletePort(key, modificationToken)
        }

        for (sheet in sheets.values) {
            sheet.clear(modificationToken)
        }
    }

    fun setSimulationValue(name: String, value: Any, token: ModificationToken) {
        simulationValueMap[name] = value
        if (simulationMode_) {
            portMap[name]?.notifyValueChanged(value, token)
        }
    }
}