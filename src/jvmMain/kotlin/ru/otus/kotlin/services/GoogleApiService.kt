package ru.otus.kotlin.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import ru.otus.kotlin.models.AuthGoogleServiceRequest
import ru.otus.kotlin.models.AuthGoogleServiceResponse
import ru.otus.kotlin.models.AuthRequest

class GoogleApiService private constructor(val key: String) {
    private val host = "https://identitytoolkit.googleapis.com"
    private val client = HttpClient {
        install(Logging)
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
                serializeNulls()
            }
        }
    }

    suspend fun signInWithPassword(req: AuthRequest): AuthGoogleServiceResponse {
        val response = client.post("$host/v1/accounts:signInWithPassword?key=$key") {
            contentType(ContentType.Application.Json)
            val request = AuthGoogleServiceRequest(req.email, req.password)
            setBody(request)
        }

        return response.call.body<AuthGoogleServiceResponse>().also {
            println("Bearer " + it.idToken)
        }
    }

    companion object {
        private lateinit var INSTANCE: GoogleApiService

        /**
         * must be called after init method
         */
        fun getInstance(): GoogleApiService {
            if (!::INSTANCE.isInitialized) {
                throw IllegalStateException("getInstance called before init method")
            }
            return INSTANCE
        }

        fun init(key: String): GoogleApiService {
            INSTANCE = GoogleApiService(key)
            return INSTANCE
        }
    }
}
