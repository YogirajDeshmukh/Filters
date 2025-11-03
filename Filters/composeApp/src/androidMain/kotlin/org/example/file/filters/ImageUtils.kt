package org.example.file.filters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri


import java.nio.ByteBuffer
import java.nio.ByteOrder

fun getBitmapFromGallery(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}




fun preprocessBitmap(bitmap: Bitmap, inputSize: Int): ByteBuffer {
    // Resize the bitmap
    val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

    // Allocate ByteBuffer (4 bytes per float, 3 channels RGB)
    val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
    byteBuffer.order(ByteOrder.nativeOrder())

    val intValues = IntArray(inputSize * inputSize)
    resized.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)

    for (pixel in intValues) {
        val r = (pixel shr 16 and 0xFF).toFloat() / 255f
        val g = (pixel shr 8 and 0xFF).toFloat() / 255f
        val b = (pixel and 0xFF).toFloat() / 255f

        byteBuffer.putFloat(r)
        byteBuffer.putFloat(g)
        byteBuffer.putFloat(b)
    }

    return byteBuffer
}
