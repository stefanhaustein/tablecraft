package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.model.builtin.BuiltinFunctions
import org.kobjects.tablecraft.pluginapi.*
import org.kobjects.tablecraft.plugins.mqtt.MqttPlugin
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

object Model {

    val STORAGE_FILE = File("storage/data.tc")

    var modificationTag: Long = 0
    var simulationMode_: Boolean = false

    val sheets = mutableMapOf<String, Sheet>("Sheet1" to Sheet("Sheet1"))
    val lock = ReentrantLock()
    val listeners = mutableSetOf<() -> Unit>()

    val functionMap = mutableMapOf<String, OperationSpec>()
    val plugins = mutableListOf<Plugin>()

    val portSpecMap = mutableMapOf<String, PortSpec>()
    val portInstanceMap = mutableMapOf<String, PortInstance>()

    val inputPortMap = mutableMapOf<String, MutableSet<OperationHost>>()
    val simulationValueMap = mutableMapOf<String, Any>()

    val svgs = SvgManager(File("src/main/resources/static/img"))


    init {
        addPlugin(BuiltinFunctions)
        addPlugin(Pi4jPlugin())
        addPlugin(svgs)
        addPlugin(MqttPlugin)

        withLock { runtimeContext ->
            loadData(STORAGE_FILE.readText(), runtimeContext)
        }
    }

    fun addPlugin(plugin: Plugin) {
        plugins.add(plugin)
        for (function in plugin.operationSpecs) {
            functionMap[function.name] = function
        }
        for (portSpec in plugin.portSpecs) {
            portSpecMap[portSpec.name] = portSpec
        }
    }

    fun setSimulationMode(value: Boolean) {
        simulationMode_ = value
        for ((name, ports) in inputPortMap) {
            val value = simulationValueMap[name]
            if (value != null) {
                for (port in ports) {
                    port.notifyValueChanged(value)
                }
            }
        }
    }


    @OptIn(ExperimentalContracts::class)
    inline fun <T> withLock(action: (RuntimeContext) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        return lock.withLock {
            val runtimeContext = RuntimeContext()
            action(runtimeContext)
        }
    }

    fun loadData(data: String, runtimeContext: RuntimeContext) {
        try {
            val toml = TomsonParser.parse(data)
            for ((key, map) in toml) {
                if (key.isEmpty()) {
                    setSimulationMode(map["simulationMode"] as Boolean? ?: false)
                } else if (key.startsWith("sheets.") && key.endsWith(".cells")) {
                    val name = key.substringAfter("sheets.").substringBeforeLast(".cells")
                    val sheet = Sheet(name)
                    sheets[name] = sheet
                    sheet.parseToml(map)
                } else if (key == "ports") {
                    for ((name, value) in map) {
                        try {
                            definePort(name, value as Map<String, Any>)
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
            sheet.updateAll(runtimeContext)
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

        writer.write(serializePorts(tag))

        writer.write("\n")

        for (sheet in sheets.values) {
            writer.write(sheet.serialize(tag, forClient))
            writer.write("\n")
        }
    }


    fun save(runtimeContext: RuntimeContext) {
        STORAGE_FILE.mkdirs()
        val writer = FileWriter(STORAGE_FILE)
        serialize(writer)
        writer.close()
    }


    fun notifyContentUpdated(runtimeContext: RuntimeContext) {
        modificationTag = runtimeContext.tag
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
        sb.append('\n')
        for (plugin in plugins) {
            for (portSpec in plugin.portSpecs) {
                if (tag <= 0) {
                    sb.append(portSpec.name).append(": ")
                    portSpec.toJson(sb)
                    sb.append('\n')
                }
            }
        }
        return if (sb.isEmpty()) "" else "[functions]\n\n$sb"
    }

    fun serializePorts(tag: Long): String {
        val sb = StringBuilder()
        for (port in portInstanceMap.values) {
            if (port.tag > tag) {
                sb.append(port.name).append(": ")
                port.toJson(sb)
                sb.append('\n')
            }
        }
        return if (sb.isEmpty()) "" else "[ports]\n\n$sb"
    }

    fun deletePort(name: String, runtimeContext: RuntimeContext) {
        portInstanceMap[name] = PortInstance.Tombstone(name, runtimeContext.tag)
    }

    fun definePort(name: String?, jsonSpec: Map<String, Any>, runtimeContext: RuntimeContext? = null) {
        val previousName = jsonSpec["previousName"] as String?

        if (!previousName.isNullOrBlank()) {
            require(runtimeContext != null)
            deletePort(previousName, runtimeContext)
        }

        if (!name.isNullOrBlank()) {
            val type = jsonSpec["type"].toString()
            val configuration = mutableMapOf<String, Any>()
            val jsonConfig = jsonSpec["configuration"] as Map<String, Any>

            val portSpec = portSpecMap[type]!!
            for (paramSpec in portSpec.parameters) {
                if (paramSpec.kind == ParameterKind.CONFIGURATION) {
                   configuration[paramSpec.name] =
                       paramSpec.type.fromString(jsonConfig[paramSpec.name].toString())
                }
            }
            val port = portSpec.createFn(name, configuration, runtimeContext?.tag ?: 0L)
            portInstanceMap[name] = port
            for (f in port.operationSpecs) {
               functionMap[f.name] = f
            }
        }
    }

    fun clearAll(runtimeContext: RuntimeContext) {
        for (key in portInstanceMap.keys.toList()) {
            deletePort(key, runtimeContext)
        }

        for (sheet in sheets.values) {
            sheet.clear(runtimeContext)
        }
    }

    fun setSimulationValue(name: String, value: Any, runtimeContext: RuntimeContext) {
        simulationValueMap[name] = value
        for (host in inputPortMap[name] ?: emptySet<OperationHost>()) {
            host.notifyValueChanged(value)
        }
    }
}