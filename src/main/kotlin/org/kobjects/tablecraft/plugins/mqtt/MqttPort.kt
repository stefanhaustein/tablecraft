package org.kobjects.tablecraft.plugins.mqtt

import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import org.kobjects.tablecraft.pluginapi.*

class MqttPort(
    val name: String,
     val configuration: Map<String, Any>,
     val tag: Long
)  {

    var client: MQTTClient? = null
    var exception: Exception? = null
    val listeners = mutableMapOf<String, MutableSet<OperationHost>>()


    init {
        Thread {
            try {
                client = MQTTClient(MQTTVersion.MQTT5, configuration["address"].toString(), configuration["port"]!! as Int, null) {
                    println("MQTT packet received: $it")
                }
                for (topic in listeners.keys) {
                    client!!.subscribe(listOf(Subscription(topic)))
                }
                client!!.run()
            } catch (e: Exception) {
                e.printStackTrace()
                exception = e
            }
        }.start()
    }

    val operationSpecs: List<OperationSpec> = listOf(
        OperationSpec(
            OperationKind.FUNCTION,
            Type.TEXT,
            "$name.subscribe",
            "Subscribe to the given topic and receive update messages",
            listOf(ParameterSpec("topic", ParameterKind.CONFIGURATION, Type.TEXT, true)),
            tag
        ) {
            MqttSubscription(this, it)
        },
        OperationSpec(
            OperationKind.FUNCTION,
            Type.TEXT,
        "$name.publish",
        "Publish / update a message for a given topic",
            listOf(
                ParameterSpec("topic", ParameterKind.CONFIGURATION, Type.TEXT, true),
                ParameterSpec("payload", ParameterKind.RUNTIME, Type.TEXT, true),
                ),
            tag
        ) {
            MqttPublisher(this, it)
        }
    )

    fun addListener(topic: String, operationHost: OperationHost) {
        val hosts = listeners.getOrPut(topic) {
            client?.subscribe(listOf(Subscription(topic)))
            mutableSetOf()
        }
        hosts.add(operationHost)
    }

    fun removeListener(topic: String, operationHost: OperationHost) {
        val hosts = listeners[topic]
        if (hosts != null) {
            hosts.remove(operationHost)
            if (hosts.isEmpty()) {
                client?.unsubscribe(listOf(topic))
            }
        }
    }
}