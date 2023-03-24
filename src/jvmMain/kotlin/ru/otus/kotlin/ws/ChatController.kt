package ru.otus.kotlin.ws

import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import ru.otus.kotlin.models.AuthRequest
import ru.otus.kotlin.models.SendTextMessageRequest
import ru.otus.kotlin.models.SetReactionRequest
import ru.otus.kotlin.models.User
import java.util.*

/**
 * variable stores all sessions where users are connected to backend.
 */
internal val sessions = Collections.synchronizedMap<WebSocketSession, UserSessionData>(LinkedHashMap())

/**
 * Current ws flow has its own simple format
 * - 1st line - current api version (number)
 * - 2nd line - endpoint or request type (@see [Endpoints])
 * - 3rd line - current api version (number)
 */
suspend fun WebSocketSession.wsChat(user: User) {
    // Init user if it's in chat room
    logIn(user)

    incoming.receiveAsFlow().filterIsInstance<Frame.Text>().mapNotNull {
        val text = it.readText().splitToSequence('\n', limit = 3).toList()

        // Top level try-catch to keep the flow
        try {
            if (text[0] == "0") {
                handleV1(user, text[1], text[2])
            } else {
                val error = Response(null, "Incorrect api version (${text[0]})", ResponseStatus.ERROR)
                response(error)
            }
        } catch (_: ClosedReceiveChannelException) {
            sessions.clear()
        } catch (e: Exception) {
            e.printStackTrace()

            val error = Response(null, e.message, ResponseStatus.ERROR)
            response(error)
        }
    }.collect()
}


suspend fun WebSocketSession.handleV1(user: User, name: String, data: String) {
    when (enumValueOf<Endpoints>(name)) {
        // Create an account
        Endpoints.auth -> {
            val req = gson.fromJson(data, AuthRequest::class.java)
            auth(req)
        }
        // Exit from channel
        Endpoints.logout -> logout()
        // Set/remove reaction from message
        Endpoints.set_reaction -> {
            val req = gson.fromJson(data, SetReactionRequest::class.java)

            setReaction(user, req)
        }

        // Send message to all users
        Endpoints.send_message -> {
            val req = gson.fromJson(data, SendTextMessageRequest::class.java)

            sendMessage(user, req)
        }
    }
}

suspend fun <T> WebSocketSession.response(data: T): Unit = response(Response(data))
suspend fun WebSocketSession.response(data: Response<*>): Unit = send(gson.toJson(data))
