@file:Suppress("UnusedReceiverParameter")

package ru.otus.kotlin.ws

import io.ktor.websocket.*
import ru.otus.kotlin.firebase.FirebaseService
import ru.otus.kotlin.models.AuthRequest
import ru.otus.kotlin.models.SendTextMessageRequest
import ru.otus.kotlin.models.SetReactionRequest
import ru.otus.kotlin.models.User


suspend fun WebSocketSession.auth(req: AuthRequest) {
    val response = FirebaseService.createUser(req)

    response(response)
}

suspend fun WebSocketSession.logIn(user: User) {
    val listeners = listOf(
        FirebaseService.messages { msg ->
            sessions.keys.forEach {
                it.response(msg)
            }
        },
        FirebaseService.users { msg ->
            sessions.keys.forEach {
                it.response(msg)
            }
        }
    )
    sessions[this] = UserSessionData(this, user, listeners)

    send("Please enter your name (name: <name>) and message (msg: <message>) or close")
}

suspend fun WebSocketSession.sendMessage(user: User, req: SendTextMessageRequest) {
    val message = FirebaseService.sendTextMessage(user, req)
    sessions.keys.forEach {
        it.response(message)
    }
}

suspend fun WebSocketSession.setReaction(user: User, req: SetReactionRequest) {
    val message = FirebaseService.setReaction(user, req)
    sessions.keys.forEach {
        it.response(message)
    }
}

suspend fun WebSocketSession.logout() {
    sessions[this]?.listeners?.forEach {
        it.dispose()
    }
    sessions.remove(this)

    response(null)
    close(CloseReason(CloseReason.Codes.NORMAL, "Closed by request"))
}
