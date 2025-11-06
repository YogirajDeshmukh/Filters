package com.example.filter.domain

expect class ImageEnhancer() {
    fun attachContext(context: Any)
    fun initModel()
    fun enhance(imageData: ByteArray): ByteArray
    fun close()
}
