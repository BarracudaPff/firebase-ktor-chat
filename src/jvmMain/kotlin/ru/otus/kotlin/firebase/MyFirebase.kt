package ru.otus.kotlin.firebase

import java.io.InputStream


private fun Any.readResource(name: String): InputStream {
    val stream = this::class.java.classLoader.getResourceAsStream(name)
    requireNotNull(stream) {
        "Can't find passed file `$name` for `$this` class loader"
    }
    return stream
}
