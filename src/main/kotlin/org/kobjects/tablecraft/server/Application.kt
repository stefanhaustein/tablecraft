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
import org.kobjects.tablecraft.json.JsonParser
import org.kobjects.tablecraft.json.toJson
import org.kobjects.tablecraft.model.Integrations
import org.kobjects.tablecraft.model.Model
import org.kobjects.tablecraft.model.Ports
import java.io.File
import java.io.StringWriter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain.main(args)
}

fun Application.module() {
    routing {
        post("/update/{cell}") {
            val cell = call.parameters["cell"]!!
            val text = call.receiveText()
            val json = JsonParser.parseObject(text)
            Model.applySynchronizedWithToken { token ->
                Model.getOrCreate(cell).setJson(json, token)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/simulationMode") {
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val value = JsonParser.parse(jsonText)
            Model.applySynchronizedWithToken { token ->
                Model.setSimulationMode(value as Boolean, token)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/portSimulation") {
            val name = call.request.queryParameters["name"]!!
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val value = JsonParser.parse(jsonText)
            Model.applySynchronizedWithToken { token ->
                Model.setSimulationValue(name, value, token)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/updatePort") {
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val jsonSpec = JsonParser.parseObject(jsonText)
            val name = jsonSpec["name"] as String?
            Model.applySynchronizedWithToken { token ->
                Model.ports.definePort(name, jsonSpec, token)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/updateIntegration") {
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val jsonSpec = JsonParser.parseObject(jsonText)
            val name = jsonSpec["name"] as String
            Model.applySynchronizedWithToken { token ->
                Model.integrations.defineIntegration(name, jsonSpec, token)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/sheet") {
            val jsonText = call.receiveText()
            println("Received JSON: $jsonText")
            val jsonSpec = JsonParser.parseObject(jsonText)
            val name = jsonSpec["name"] as String?
            Model.applySynchronizedWithToken { token ->
                Model.updateSheet(name, jsonSpec, token)
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/upload") {
            val fileItem = call.receiveMultipart().readPart() as PartData.FileItem
            val data = fileItem.provider().toByteArray().toString(Charsets.UTF_8)
            Model.applySynchronizedWithToken {
                Model.clearAll(it)
                Model.loadData(data, it)
                it.symbolsChanged = true
                it.formulaChanged = true
            }
            call.respond(HttpStatusCode.OK, null)
        }
        post("/loadExample") {
            val name = call.receiveText()
            val exampleFile = File("src/main/resources/examples", name + ".tc")
            val data = exampleFile.readText()
            Model.applySynchronizedWithToken {
                Model.clearAll(it)
                Model.loadData(data, it)
                it.symbolsChanged = true
                it.formulaChanged = true
            }
        }
        post("/clearAll") {
            Model.applySynchronizedWithToken {
                Model.clearAll(it)
                it.symbolsChanged = true
                it.formulaChanged = true
            }
        }
        get("/data") {
            val rawTag = call.request.queryParameters["tag"]?.toLong()
            val forClient = rawTag != null
            val tag = rawTag ?: -1

            if (tag >= Model.modificationTag) {
                suspendCoroutine<Unit> { continuation ->
                    Model.applySynchronizedWithToken {
                        Model.listeners.add {
                            continuation.resume(Unit)
                        }
                    }
                }
            }
            val result = Model.applySynchronized {
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
        get("/rest") {
            val json = Model.restValues.toJson()
            call.respondText(json, ContentType.Application.Json, HttpStatusCode.OK)
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
