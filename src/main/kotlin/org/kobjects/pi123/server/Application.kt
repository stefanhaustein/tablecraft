package org.kobjects.pi123.server

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.Sheet
import java.io.File

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    routing {
        post("/update/{cell}") {
            val cell = call.parameters["cell"]!!
            val text = call.receiveText()
            Model.set(cell, text)
            call.respond(HttpStatusCode.OK, null)
            Model.save()
        }
        get("/sheet/{name}/computed") {
            val sheet = Model.sheets[call.parameters["name"]!!]!!
            sheet.update()
            call.respondText(sheet.serializeValues(Sheet.ValueType.COMPUTED_VALUE), ContentType.Application.Json, HttpStatusCode.OK,)
        }
        get("/sheet/{name}/formulas") {
            call.respondText(Model.sheets[call.parameters["name"]!!]!!.serializeValues(Sheet.ValueType.FORMULA), ContentType.Application.Json, HttpStatusCode.OK,)
        }
        /* get("/") {
             call.respondText("Hello World!")
         }*/
        // Static plugin. Try to access `/static/index.html`
        staticFiles("/", File("src/main/resources/static"))
        //staticResources("/", "static")
    }
}
