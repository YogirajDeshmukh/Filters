package com.example.filter.domain

open class BackgroundRemovalUseCase(
    private val remover: suspend (ImageData, BackgroundOption) -> ImageData
) {
    suspend fun execute(input: ImageData, option: BackgroundOption): ImageData {
        return remover(input, option)
    }
}
