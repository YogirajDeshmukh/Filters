package com.example.filter


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.filter.model.ESRGANModelInterpreter
import java.io.File
import java.io.FileOutputStream

class ImageProcessorImpl(
    private val context: Context
) : ImageProcessor {

    override suspend fun enhanceImage(imagePath: String): String {
        val model = ESRGANModelInterpreter(context)

        val originalBitmap = BitmapFactory.decodeFile(imagePath)
        val enhancedBitmap = model.enhanceImage(originalBitmap)

        // Save enhanced image in cache directory
        val outputFile = File(context.cacheDir, "enhanced_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use {
            enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }

        return outputFile.absolutePath
    }
}
