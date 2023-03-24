package ru.otus.kotlin.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import java.io.InputStream

val MyFirebase = createApplicationPlugin(name = "MyFirebase") {
    on(MonitoringEvent(ApplicationStarted)) { application ->
        application.log.info("Server is started!")

        val serviceJsonPath = application.environment.config.property("ktor.firebase.service_file")
        val serviceAccount = this.readResource(serviceJsonPath.getString())

        val databaseUrl = application.environment.config.property("ktor.firebase.database_url")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl(databaseUrl.getString().let { "https://" + it.removePrefix("https://") })
            .build()


//        application.environment.config.propertyOrNull("ktor.firebase.web_key")?.let {
//            GoogleApiService.init(it.getString())
//        }

        FirebaseApp.initializeApp(options)
    }
}

private fun Any.readResource(name: String): InputStream {
    val stream = this::class.java.classLoader.getResourceAsStream(name)
    requireNotNull(stream) {
        "Can't find passed file `$name` for `$this` class loader"
    }
    return stream
}
