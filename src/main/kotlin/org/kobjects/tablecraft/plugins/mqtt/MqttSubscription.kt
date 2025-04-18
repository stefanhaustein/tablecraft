package org.kobjects.tablecraft.plugins.mqtt

import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.Operation
import org.kobjects.tablecraft.pluginapi.StatefulOperation

class MqttSubscription(val port: MqttPort, configuration: Map<String, Any>) : StatefulOperation {
    val topic = configuration["topic"].toString()
    var host: OperationHost? = null

    override fun attach(host: OperationHost) {
        port.addListener(topic, host)
        this.host = host
    }

    override fun apply(params: Map<String, Any>): Any {
        return ""
    }

    override fun detach() {
        port.removeListener(topic, host!!)
    }

}