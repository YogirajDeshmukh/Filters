package com.example.filter.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImagePreprocessor {


    // Prepares the image for the ESRGAN model: Pads rectangular images to square Resizes to the model input size
    //Makes Normalizes pixel values (0–255 → 0–1)

    fun preprocess(bitmap: Bitmap, inputSize: Int = 128): ByteBuffer {
        val squareBitmap = padToSquare(bitmap)
        val resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, inputSize, inputSize, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in intValues) {
            // Extract RGB values
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            // Put normalized floats in order RGB
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    private fun padToSquare(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        if (width == height) return src

        val size = maxOf(width, height)
        val paddedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(paddedBitmap)
        canvas.drawColor(Color.BLACK)

        val left = (size - width) / 2
        val top = (size - height) / 2
        canvas.drawBitmap(src, left.toFloat(), top.toFloat(), null)

        return paddedBitmap
    }
}
