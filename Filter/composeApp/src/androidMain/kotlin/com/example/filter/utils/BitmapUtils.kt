package com.example.filter.utils

import android.graphics.Bitmap
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

object BitmapUtils {

    fun convertTensorToBitmap(outputBuffer: TensorBuffer, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val floatArray = outputBuffer.floatArray
        val pixels = IntArray(width * height)

        for (i in 0 until width * height) {
            // Output values are typically already in [0, 1] range.
            var r = floatArray[i * 3]
            var g = floatArray[i * 3 + 1]
            var b = floatArray[i * 3 + 2]

            // Fix potential out-of-range values (some ESRGAN variants output -1..1)
            if (r < 0 || g < 0 || b < 0) {
                r = (r + 1f) / 2f
                g = (g + 1f) / 2f
                b = (b + 1f) / 2f
            }

            // Clamp and scale to 0–255
            val rr = (r.coerceIn(0f, 1f) * 255).toInt()
            val gg = (g.coerceIn(0f, 1f) * 255).toInt()
            val bb = (b.coerceIn(0f, 1f) * 255).toInt()

            pixels[i] = (0xFF shl 24) or (rr shl 16) or (gg shl 8) or bb
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
