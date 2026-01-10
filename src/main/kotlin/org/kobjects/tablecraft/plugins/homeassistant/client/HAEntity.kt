package org.kobjects.tablecraft.plugins.homeassistant.client

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class HAEntity(
    val client: HomeAssistantClient,
    val json: JsonObject,
) {
    val id: String = json["entity_id"]!!.jsonPrimitive.content

    val kind: Kind
        get() = Kind.entries.find { it.name.lowercase() == id.substring(0, id.indexOf('.')) } ?: Kind.UNRECOGNIZED

    val category: String?
        get() = json["entity_category"]?.jsonPrimitive?.contentOrNull

    val deviceId = json["device_id"]?.jsonPrimitive?.contentOrNull
    val device: HADevice? = client.devices[deviceId]
    val disabledBy = json["disabled_by"]?.jsonPrimitive?.contentOrNull

    val friendlyName: String?
        get() = state?.json?.get("attributes")?.jsonObject?.get("friendly_name")?.jsonPrimitive?.contentOrNull

    val state: HAEntityState?
        get() = client.entityStates[id]

    val description: String
        get() = (friendlyName?:"") + ".debug:\n" + Json {prettyPrint = true}.encodeToString(json) +
                "\n\nstate:" + Json {prettyPrint = true}.encodeToString(state?.json)

    override fun toString(): String = id + " - " + category + " - " + json

    enum class Kind {
        BUTTON,
        BINARY_SENSOR,
        LIGHT,
        SELECT,
        SENSOR,
        UPDATE,

        UNRECOGNIZED,
    }
}