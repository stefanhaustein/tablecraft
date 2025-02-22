package org.kobjects.tablecraft.server

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.html.dom.serialize
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.kobjects.tablecraft.json.JsonParser
import org.kobjects.tablecraft.model.Model
import java.io.File
import java.io.StringWriter
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
                Model.save(it)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/updatePort") {
            val name = call.request.queryParameters["name"]
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val jsonSpec = JsonParser.parseObject(jsonText)
            Model.withLock {
                Model.definePort(name, jsonSpec, it)
                Model.notifyContentUpdated(it)
                Model.save(it)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/upload") {
            val fileItem = call.receiveMultipart().readPart() as PartData.FileItem
            val data = fileItem.provider().toByteArray().toString(Charsets.UTF_8)
            Model.withLock {
                Model.clearAll(it)
                Model.loadData(data, it)
                Model.save(it)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        get("/data") {
            val rawTag = call.request.queryParameters["tag"]?.toLong()
            val forClient = rawTag != null
            val tag = rawTag ?: -1

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
                val writer = StringWriter()
                if (forClient) {
                    writer.write("tag = ${Model.modificationTag}\n\n")
                }
                Model.serialize(writer, forClient, tag)
                writer.close()
                writer.toString()
            }
            call.respondText(result, ContentType.Text.Plain, HttpStatusCode.OK,)
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
