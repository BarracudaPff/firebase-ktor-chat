package ru.otus.kotlin

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.html.*
import org.slf4j.event.Level
import ru.otus.kotlin.firebase.*

fun HTML.index() {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
        div {
            id = "root"
        }
        script(src = "/static/firebase-ktor-chat.js") {}
    }
}

// Application.module linked in application.conf file
fun main(args: Array<String>): Unit = EngineMain.main(args)

//fun main() {
//    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
//        module()
//    }.start(wait = true)
//}

@Suppress("unused")
fun Application.module() {
    // Our custom plugin
    install(MyFirebase)

    // Used only in REST
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }
    // Required for chat
    install(WebSockets)
    // Simple logging for requests/responses
    install(CallLogging) {
        level = Level.INFO
    }

    routing {
        route("api/v1") {
            // Chat is available only for authorized users
            webSocket("ws/chat") {
                // TODO: chat here
            }
        }
    }

    routing {
        // Simple debug route
        get("/") {
            call.respondHtml(HttpStatusCode.OK, HTML::index)
        }
        // Route to check if content negotiation is working
        get("/json/serialization-check") {
            call.respond(mapOf("hello" to "world!"))
        }
        // Sample how to upload static files
        static("/static") {
            resources()
        }
    }
}
