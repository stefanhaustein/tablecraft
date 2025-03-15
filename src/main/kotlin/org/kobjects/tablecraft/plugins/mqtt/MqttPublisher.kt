package org.kobjects.tablecraft.plugins.mqtt

import io.github.davidepianca98.mqtt.packets.Qos
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.Operation
import org.kobjects.tablecraft.pluginapi.StatefulOperation

class MqttPublisher(val port: MqttPort, config: Map<String, Any>) : StatefulOperation {

    val topic = config["topic"].toString()

    override fun attach(host: OperationHost) {

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun apply(params: Map<String, Any>): Any {
        val payload = params["payload"].toString()
        port.client?.publish(false, Qos.EXACTLY_ONCE, topic, payload.toByteArray().toUByteArray())
        return payload
    }

    override fun detach() {

    }


}