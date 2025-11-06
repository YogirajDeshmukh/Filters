package com.example.filter.utils


import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

object BitmapUtils {

    fun convertOutputToBitmap(outputBuffer: Array<Array<Array<FloatArray>>>): Bitmap {
        val height = outputBuffer[0].size
        val width = outputBuffer[0][0].size
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val r = (outputBuffer[0][y][x][0] * 255).toInt().coerceIn(0, 255)
                val g = (outputBuffer[0][y][x][1] * 255).toInt().coerceIn(0, 255)
                val b = (outputBuffer[0][y][x][2] * 255).toInt().coerceIn(0, 255)
                val color = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }
}
