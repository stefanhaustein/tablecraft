package org.kobjects.tablecraft.server

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.dom.serialize
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.kobjects.tablecraft.json.JsonParser
import org.kobjects.tablecraft.model.Model
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    routing {
        post("/update/{cell}") {
            val cell = call.parameters["cell"]!!
            val text = call.receiveText()
            Model.withLock {
                Model.set(cell, text, it)
                Model.save()
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/updatePort") {
            val name = call.request.queryParameters["name"]
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val jsonSpec = JsonParser.parse(jsonText)
            Model.withLock {
                Model.definePort(name, jsonSpec as Map<String, Any>, it)
                Model.notifyContentUpdated(it)
                Model.save()
            }
            call.respond(HttpStatusCode.OK, null)
        }
        get("/sheet/{name}") {
            val name = call.parameters["name"]
            val tag = call.request.queryParameters["tag"]!!.toLong()

            if (tag >= Model.modificationTag) {
                suspendCoroutine<Unit> { continuation ->
                    Model.withLock {
                        Model.listeners.add {
                            continuation.resume(Unit)
                        }
                    }
                }
            }
            val result = Model.withLock {
                val sheet = Model.sheets[name]!!
                Model.serializeFunctions(tag) + "\n" + Model.serializePorts(tag) + "\n" + sheet.serialize(tag, true)
            }
            call.respondText("tag = ${Model.modificationTag}\n$result", ContentType.Text.Plain, HttpStatusCode.OK,)
        }
        get("img/{name...}") {
            val path = call.parameters.getAll("name")!!.joinToString("/")
            println("Svg requested: $path; available: ${Model.svgs.map}")
            val svg = Model.svgs.map["img/$path"]
            println("Found: $svg")

            val parameterMap = call.request.queryParameters.entries().map { Pair(it.key, it.value.first()) }.toMap()
            val parameterized = svg!!.parameterized(parameterMap)

            call.respondText(parameterized.documentElement.serialize(), ContentType.Image.SVG)
        }

        /* get("/") {
             call.respondText("Hello World!")
         }*/
        // Static plugin. Try to access `/static/index.html`
        staticFiles("/", File("src/main/resources/static"))
        //staticResources("/", "static")
    }
}
