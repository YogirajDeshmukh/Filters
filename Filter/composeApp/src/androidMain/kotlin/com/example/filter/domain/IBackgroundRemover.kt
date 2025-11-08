package com.example.filter.domain


import android.graphics.Bitmap
import com.example.filter.domain.BackgroundOption

interface IBackgroundRemover {
    suspend fun removeBackground(
        input: Bitmap,
        option: BackgroundOption,
        solidColor: Int = 0xFFFFFFFF.toInt(),
        backgroundImage: Bitmap? = null
    ): Bitmap
}
