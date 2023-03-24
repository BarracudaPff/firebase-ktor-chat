package ru.otus.kotlin

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {

    routing {
        route("api/v1") {
            webSocket("ws/chat") {
                // TODO: chat here
            }
        }
    }

    routing {
        get("/json/serialization-check") {
            call.respond(mapOf("hello" to "world!"))
        }
        static("/static") {
            resources()
        }
    }
}
