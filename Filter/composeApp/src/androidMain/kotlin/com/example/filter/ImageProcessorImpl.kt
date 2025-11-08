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
        // Load ESRGAN model
        val model = ESRGANModelInterpreter(context)

        // Decode original image
        val originalBitmap = BitmapFactory.decodeFile(imagePath)
            ?: throw IllegalArgumentException("Unable to decode image at: $imagePath")

        // Enhance image using ESRGAN
        val enhancedBitmap: Bitmap = model.enhanceImage(originalBitmap)

        // 🔹 Save temporarily (in app cache)
        val tempFile = File(context.cacheDir, "enhanced_temp_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { out ->
            enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            out.flush()
        }

        // Return the temporary file path
        return tempFile.absolutePath
    }
}
