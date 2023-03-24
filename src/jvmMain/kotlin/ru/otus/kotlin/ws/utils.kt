package ru.otus.kotlin.ws

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.websocket.*
import ru.otus.kotlin.models.User

fun interface Disposable {
    fun dispose()
}

enum class ResponseStatus {
    SUCCESS,
    ERROR,
}

data class Response<T>(
    val data: T,
    val error: String? = null,
    val status: ResponseStatus = ResponseStatus.SUCCESS,
)

data class UserSessionData(
    val session: WebSocketSession,
    val user: User,
    val listeners: List<Disposable>,
)

@Suppress("EnumEntryName")
enum class Endpoints {
    auth,
    logout,
    send_message,
    set_reaction,
}

val gson: Gson = GsonBuilder().apply {
    setPrettyPrinting()
    serializeNulls()
}.create()
