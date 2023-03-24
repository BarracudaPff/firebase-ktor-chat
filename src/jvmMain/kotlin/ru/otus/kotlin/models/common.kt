package ru.otus.kotlin.models

import io.ktor.server.auth.*

/**
 * Model have Firebase optional key
 */
abstract class FBModel {
    var key: String? = null
}

fun <T : FBModel> T.withKey(key: String): T {
    this.key = key
    return this
}

/**
 * App Models below
 */

data class User(
    val id: String,
    val name: String,
    val email: String?,
) : Principal, FBModel()

data class Message(
    val text: String,
    val author: String,
    val date: Long,
    val type: Type,
    val reactions: Map<String, Set<String>>,
) : FBModel() {
    enum class Type {
        Message
    }
}
