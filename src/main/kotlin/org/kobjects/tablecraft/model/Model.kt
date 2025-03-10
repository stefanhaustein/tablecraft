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
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object Model : ModelInterface {

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

    private val lock = ReentrantLock()

    init {
        addPlugin(BuiltinFunctions)
        addPlugin(Pi4jPlugin(this))
        addPlugin(svgs)
        // addPlugin(MqttPlugin)

        applySynchronizedWithToken { runtimeContext ->
            runtimeContext.loading = true
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
                    sheet.parseToml(map, token)
                } else if (key == "ports") {
                    for ((name, value) in map) {
                        try {
                            definePort(name, value as Map<String, Any>, token)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else if (key == "simulationValues") {
                    simulationValueMap.putAll(map)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
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


    fun save() {
        STORAGE_FILE.mkdirs()
        val writer = FileWriter(STORAGE_FILE)
        serialize(writer)
        writer.close()
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
            if (port is OutputPort && port.expression.valueTag > tag) {
                values.append("${port.name}: ${port.expression.value.toJson()}\n")
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

    fun deletePort(name: String, token: ModificationToken) {
        token.functionSetChanged = true

        portMap[name] = InputPort(name, OperationSpec(
            OperationKind.INPUT_PORT,
            Type.TEXT,
            "tombstone",
            "",
            emptyList(),
            token.tag
        ) {
            object : OperationInstance {
                override fun attach() {
                }

                override fun apply(params: Map<String, Any>) = Unit

                override fun detach() {
                }
            }
        }, emptyMap(), token)
    }

    fun definePort(name: String?, jsonSpec: Map<String, Any>, token: ModificationToken): Port? {
        token.functionSetChanged = true

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

            val specification = Model.functionMap[type]!!
            val port = when (specification.kind) {
                OperationKind.INPUT_PORT -> InputPort(name, specification, config, token)
                OperationKind.OUTPUT_PORT -> OutputPort(name, specification, config, jsonSpec["expression"] as String, token)
                else -> throw IllegalArgumentException("Operation specification $specification does not specify a port.")
            }
            portMap[name] = port
            return port
        }

        return null
    }

    fun clearAll(modificationToken: ModificationToken) {
        modificationToken.functionSetChanged = true

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
    @OptIn(ExperimentalContracts::class)
    override fun <T> applySynchronizedWithToken(action: (ModificationToken) -> T): T {
        return lock.withLock {
            val modificationToken = ModificationToken()

            val result = action(modificationToken)

            if (modificationToken.functionSetChanged) {

            }

            var anyChanged = modificationToken.formulaChanged || modificationToken.formulaChanged

            while (modificationToken.refreshRoots.isNotEmpty()) {
                val first = modificationToken.refreshRoots.first()
                modificationToken.refreshRoots.remove(first)
                if (first.updateValue(modificationToken.tag)) {
                    anyChanged = true
                    for (dep in first.dependencies) {
                        if (dep.dependsOn.size == 1) {
                            modificationToken.refreshNodes.remove(dep)
                            modificationToken.refreshRoots.add(dep)
                        } else {
                            modificationToken.addRefresh(dep)
                        }
                    }
                }
            }

            for (dep in modificationToken.refreshNodes) {
                modificationToken.addAllDependencies(dep)
            }

            while (modificationToken.refreshNodes.isNotEmpty()) {
                for (node in modificationToken.refreshNodes) {
                    var found = true
                    for (dep in node.dependencies) {
                        if (modificationToken.refreshNodes.contains(dep)) {
                            found = false
                            break
                        }
                    }
                    if (found) {
                        modificationToken.refreshNodes.remove(node)
                        if (node.updateValue(modificationToken.tag)) {
                            anyChanged = true
                        }
                        break
                    }
                }
            }

            modificationTag = modificationToken.tag
            if (anyChanged) {
                for(listener in listeners) {
                    listener.invoke()
                }
                listeners.clear()
            }

            result
        }
    }

    fun <T> applySynchronized(action: () -> T) = lock.withLock(action)
}