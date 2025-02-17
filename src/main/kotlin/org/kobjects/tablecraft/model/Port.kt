package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.pluginapi.*

class Port(
    val name: String,
    val portConstructor: OperationSpec,
    override val configuration: Map<String, Any>,
    tag: Long = 0,
): OperationHost {
    val specification = OperationSpec(OperationKind.PORT_INSTANCE,
        portConstructor.returnType,
        name,
        "${portConstructor.name}; ${configuration.entries.joinToString { "${it.key}=${it.value}" }}",
        portConstructor.parameters.filter { it.kind != ParameterKind.CONFIGURATION },
        tag,
        ::createInstance,
    )

    val physicalPort = portConstructor.createFn(this) as OperationInstance
    val clients = mutableListOf<Client>()

    init {
        physicalPort.attach()
    }

    override fun notifyValueChanged(newValue: Any) {
        for (listener in clients) {
            listener.host.notifyValueChanged(newValue)
        }
    }

    fun createInstance(host: OperationHost) = Client(this, host)

    fun delete() {
        physicalPort.detach()
    }

    fun toJson(): String {
        val configJson = configuration.entries.joinToString {  "${it.key.quote()}: ${it.value.toString().quote()}" }

        return """{"name":${name.quote()}, "type":${portConstructor.name.quote()}, "configuration": {$configJson} } """
    }

    class Client(
        val port: Port,
        val host: OperationHost
    ) : OperationInstance {

        override fun attach() {
            port.clients.add(this)
        }

        override fun apply(params: Map<String, Any>) = port.physicalPort.apply(params)

        override fun detach() {
            port.clients.remove(this)
        }
    }

}