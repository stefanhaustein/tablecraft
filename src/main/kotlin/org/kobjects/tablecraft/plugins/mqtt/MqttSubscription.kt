package org.kobjects.tablecraft.plugins.mqtt

import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance

class MqttSubscription(val port: MqttPort, val host: OperationHost) : OperationInstance {

    val topic = host.configuration["topic"].toString()

    override fun attach() {
        port.addListener(topic, host)
    }

    override fun apply(params: Map<String, Any>): Any {
        return ""
    }

    override fun detach() {
        port.removeListener(topic, host)
    }

}