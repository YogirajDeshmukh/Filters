package com.example.filter

import kotlinx.coroutines.flow.Flow

interface ImageProcessor {
    suspend fun enhanceImage(imagePath: String): String
}
