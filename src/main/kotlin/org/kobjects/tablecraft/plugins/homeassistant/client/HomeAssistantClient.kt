package org.kobjects.tablecraft.plugins.homeassistant.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.associateBy

class HomeAssistantClient(
    private val host: String,
    private val port: Int,
    private val token: String
) {
    private val messageId = AtomicInteger(1)

    // Encapsulated HttpClient configuration
    private val client = HttpClient(CIO) {
        install(WebSockets)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    val areas = runBlocking { fetchAreas() }.associateBy { it.id }
    val devices = runBlocking { fetchDevices() }.associateBy { it.id }
    val entities = runBlocking { fetchEntities() }.associateBy { it.id }
    val entityStates = runBlocking { fetchEntityStates() }.associateBy { it.id }


    /**
     * Generic helper to fetch lists (Devices or Entities)
     */
    private suspend inline fun fetchJson(commandType: String): JsonElement = coroutineScope {
        val deferred = CompletableDeferred<JsonElement>()

        client.webSocket(method = HttpMethod.Get, host = host, port = port, path = "/api/websocket") {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue

                val response = Json.parseToJsonElement(frame.readText()).jsonObject

                when (response.get("type")?.jsonPrimitive?.content) {
                    "auth_required" -> sendAuth()
                    "auth_ok" -> {
                        val cmd = buildJsonObject {
                            put("id", messageId.getAndIncrement())
                            put("type", commandType)
                        }
                        send(Frame.Text(json.encodeToString(cmd)))
                    }
                    "result" -> {
                        if (response.get("success")?.jsonPrimitive?.booleanOrNull == true) {
                            val result = response.get("result")!!
                            //println("fetched result: $result")
                            deferred.complete(result)
                        } else {
                            deferred.completeExceptionally(Exception("HA Command Failed: $commandType"))
                        }
                        close()
                    }
                    "auth_invalid" -> {
                        deferred.completeExceptionally(Exception("Authentication failed"))
                        close()
                    }
                }
            }
        }
        deferred.await()
    }

    suspend fun fetchAreas() = fetchJson("config/area_registry/list").jsonArray.map { HAArea(this, it.jsonObject) }

    suspend fun fetchDevices() = fetchJson("config/device_registry/list").jsonArray.map { HADevice(this, it.jsonObject) }

    suspend fun fetchEntities() = fetchJson("config/entity_registry/list").jsonArray.map { HAEntity(this, it.jsonObject) }

    suspend fun fetchEntityStates() = fetchJson("get_states").jsonArray.map { HAEntityState(this, it.jsonObject) }

    private suspend fun DefaultClientWebSocketSession.sendAuth() {
        val authPayload = """{"type": "auth", "access_token": "$token"}"""
        send(Frame.Text(authPayload))
    }

    /**
     * Clean up the internal HTTP client
     */
    fun close() {
        client.close()
    }
}