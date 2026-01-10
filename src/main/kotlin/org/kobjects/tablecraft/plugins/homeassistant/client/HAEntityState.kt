package org.kobjects.tablecraft.plugins.homeassistant.client

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HAEntityState(
    val client: HomeAssistantClient,
    val json: JsonObject
) {

    val id: String = json["entity_id"]!!.jsonPrimitive.content
    val state: Any? = json["state"]?.jsonPrimitive?.contentOrNull
}