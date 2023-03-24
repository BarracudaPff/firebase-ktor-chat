package ru.otus.kotlin.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.otus.kotlin.models.User

class FirebaseConfig(name: String) : AuthenticationProvider.Config(name) {
    var authHeader: (ApplicationCall) -> HttpAuthHeader? = {
        it.request.parseAuth()
    }

    var authFoo: AuthenticationFunction<FirebaseToken> = {
        throw NotImplementedError()
    }

    fun validate(block: suspend ApplicationCall.(FirebaseToken) -> User?) {
        authFoo = block
    }
}

class FirebaseAuthProvider(private val config: FirebaseConfig) : AuthenticationProvider(config) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val token = config.authHeader(context.call)

        if (token == null) {
            context.challenge(FirebaseJWTAuthKey, AuthenticationFailedCause.InvalidCredentials) { challengeFunc, call ->
                challengeFunc.complete()
                call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge(realm = FIREBASE_AUTH)))
            }
            return
        }

        try {
            val principal = verifyFirebaseIdToken(context.call, token, config.authFoo)

            if (principal != null) {
                context.principal(principal)
            }
        } catch (cause: Throwable) {
            val message = cause.message ?: cause.javaClass.simpleName
            context.error(FirebaseJWTAuthKey, AuthenticationFailedCause.Error(message))
        }
    }

}

suspend fun verifyFirebaseIdToken(
    call: ApplicationCall,
    authHeader: HttpAuthHeader,
    tokenData: suspend ApplicationCall.(FirebaseToken) -> Principal?
): Principal? {
    try {
        if (authHeader.authScheme == "Bearer" && authHeader is HttpAuthHeader.Single) {
            val token = withContext(Dispatchers.IO) {
                FirebaseAuth.getInstance().verifyIdToken(authHeader.blob)
            }
            return tokenData(call, token)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }

    return null
}

fun HttpAuthHeader.Companion.bearerAuthChallenge(realm: String) = HttpAuthHeader.Parameterized(
    authScheme = "Bearer", mapOf(HttpAuthHeader.Parameters.Realm to realm)
)

const val FIREBASE_AUTH = "FIREBASE_AUTH"
const val FirebaseJWTAuthKey: String = "FirebaseAuth"

fun ApplicationRequest.parseAuth() = try {
    parseAuthorizationHeader()
} catch (e: Exception) {
    println("Can't read token")
    null
}

fun AuthenticationConfig.firebase(name: String = FIREBASE_AUTH, configure: FirebaseConfig.() -> Unit) {
    register(FirebaseAuthProvider(FirebaseConfig(name).apply(configure)))
}
