package org.kobjects.tablecraft.model

import org.kobjects.tablecraft.pluginapi.*

class Port(
    val name: String,
    val portConstructor: OperationSpec,
    val configuration: Map<String, Any>,
    val tag: Long = 0,
) {
    val specification = OperationSpec(OperationKind.PORT_INSTANCE,
        portConstructor.returnType,
        name,
        "${portConstructor.name}; ${configuration.entries.joinToString { "${it.key}=${it.value}" }}",
        portConstructor.parameters.filter { it.kind != ParameterKind.CONFIGURATION },
        tag,
        ::createInstance,
    )


    fun createInstance(host: OperationHost): OperationInstance {
        return portConstructor.createFn(object : OperationHost {
            override val configuration: Map<String, Any>
                get() = this@Port.configuration

            override fun notifyValueChanged(newValue: Any) {
                host.notifyValueChanged(newValue)
            }
        })
    }


    fun toJson(): String {
        val configJson = configuration.entries.joinToString {  "${it.key.quote()}: ${it.value.toString().quote()}" }

        return """{"name":${name.quote()}, "type":${portConstructor.name.quote()}, "configuration": {$configJson} } """
    }


}