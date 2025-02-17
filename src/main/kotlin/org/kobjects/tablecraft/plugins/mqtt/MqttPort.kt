package org.kobjects.tablecraft.plugins.mqtt

import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import org.kobjects.tablecraft.pluginapi.*

class MqttPort(
    override val name: String,
    config: Map<String, Any>
) : PortInstance {

    val client: MQTTClient
    val listeners = mutableMapOf<String, MutableSet<OperationHost>>()

    init {
        client = MQTTClient(MQTTVersion.MQTT5, config["address"].toString(), config["port"]!! as Int, null) {
            println("MQTT packet received: $it")
        }
        Thread {
            client.run()
        }.start()
    }

    override val operationSpecs: List<OperationSpec> = listOf(
        OperationSpec(
            OperationKind.PORT_INSTANCE,
            Type.TEXT,
            "$name.subscribe",
            "Subscribe to the given topic and receive update messages",
            listOf(ParameterSpec("topic", ParameterKind.CONFIGURATION, Type.TEXT, true))
        ) {
            MqttSubscription(this, it)
        },
        OperationSpec(
            OperationKind.PORT_INSTANCE,
            Type.TEXT,
        "$name.publish",
        "Publish / update a message for a given topic",
            listOf(
                ParameterSpec("topic", ParameterKind.CONFIGURATION, Type.TEXT, true),
                ParameterSpec("payload", ParameterKind.RUNTIME, Type.TEXT, true),
                )
        ) {
            MqttPublisher(this, it.configuration)
        }
    )

    fun addListener(topic: String, operationHost: OperationHost) {
        val hosts = listeners.getOrPut(topic) {
            client.subscribe(listOf(Subscription(topic)))
            mutableSetOf()
        }
        hosts.add(operationHost)
    }

    fun removeListener(topic: String, operationHost: OperationHost) {
        val hosts = listeners[topic]
        if (hosts != null) {
            hosts.remove(operationHost)
            if (hosts.isEmpty()) {
                client.unsubscribe(listOf(topic))
            }
        }
    }
}