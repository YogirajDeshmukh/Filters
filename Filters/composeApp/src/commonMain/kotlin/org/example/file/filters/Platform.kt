package org.example.file.filters

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform