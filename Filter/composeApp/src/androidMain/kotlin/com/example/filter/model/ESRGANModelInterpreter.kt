package com.example.filter.model

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ESRGANModelInterpreter(private val context: Context) {

    private lateinit var interpreter: Interpreter

    init {
        loadModel("RealESRGAN_x4plus_float.tflite")
    }

    private fun loadModel(modelName: String) {
        val buffer = loadModelFile(modelName)
        val options = Interpreter.Options().apply {
            setUseXNNPACK(true)
            setNumThreads(4)
        }
        interpreter = Interpreter(buffer, options)
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun enhanceImage(bitmap: Bitmap): Bitmap {
        val input = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
        val inputBuffer = convertBitmapToByteBuffer(input)
        val outputShape = intArrayOf(1, 512, 512, 3)
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, org.tensorflow.lite.DataType.FLOAT32)

        interpreter.run(inputBuffer, outputBuffer.buffer.rewind())
        return convertBufferToBitmap(outputBuffer, 512, 512)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * 128 * 128 * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(128 * 128)
        bitmap.getPixels(pixels, 0, 128, 0, 0, 128, 128)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }
        return buffer
    }

    private fun convertBufferToBitmap(buffer: TensorBuffer, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val arr = buffer.floatArray
        val pixels = IntArray(width * height)

        for (i in 0 until width * height) {
            val r = (arr[i * 3] * 255).toInt().coerceIn(0, 255)
            val g = (arr[i * 3 + 1] * 255).toInt().coerceIn(0, 255)
            val b = (arr[i * 3 + 2] * 255).toInt().coerceIn(0, 255)
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun close() {
        if (::interpreter.isInitialized) interpreter.close()
    }
}
