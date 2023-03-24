package ru.otus.kotlin.models

import ru.otus.kotlin.firebase.FirebaseService


////////////////////////////////////////////////////////////
/////////////////////    Message    ////////////////////////
////////////////////////////////////////////////////////////

data class SendTextMessageRequest(
    val text: String,
)

data class MessageReceive(
    val message: Message,
    val event: FirebaseService.FirebaseEvent,
    val prevChild: String?
)

////////////////////////////////////////////////////////////
////////////////////    Reactions   ////////////////////////
////////////////////////////////////////////////////////////

data class SetReactionRequest(
    val messageId: String,
    val reaction: String,
    val isEnabled: Boolean,
)

////////////////////////////////////////////////////////////
//////////////////////    User   ///////////////////////////
////////////////////////////////////////////////////////////

data class UserReceive(
    val text: User,
    val event: FirebaseService.FirebaseEvent,
    val prevChild: String?
)

////////////////////////////////////////////////////////////
//////////////////////    Auth   ///////////////////////////
////////////////////////////////////////////////////////////

data class AuthRequest(
    val name: String,
    val email: String,
    val password: String,
)

data class AuthResponse(
    val user: User,
    val token: String,
)

data class AuthGoogleServiceRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

data class AuthGoogleServiceResponse(
    val idToken: String,
    val registered: Boolean,
    val refreshToken: String,
    val expiresIn: String,

//    Fields below are not needed.
//
//    val kind: String,
//    val localId: String,
//    val email: String,
//    val displayName: String,
)
