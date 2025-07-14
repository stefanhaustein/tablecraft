package org.kobjects.tablecraft.plugins.mqtt

import io.github.davidepianca98.mqtt.packets.Qos
import org.kobjects.tablecraft.model.expression.EvaluationContext
import org.kobjects.tablecraft.pluginapi.ValueChangeListener
import org.kobjects.tablecraft.pluginapi.StatefulFunctionInstance

class MqttPublisher(val port: MqttPort, config: Map<String, Any?>) : StatefulFunctionInstance {

    val topic = config["topic"].toString()

    override fun attach(host: ValueChangeListener) {

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun apply(context: EvaluationContext, params: Map<String, Any?>): Any {
        val payload = params["payload"].toString()
        port.client?.publish(false, Qos.EXACTLY_ONCE, topic, payload.toByteArray().toUByteArray())
        return payload
    }

    override fun detach() {

    }


}