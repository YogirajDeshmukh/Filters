package org.example.file.filters

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageEnhancer(private val context: Context) {

    private val interpreter: Interpreter

    init {
        val model = loadModelFile("ESRGAN.tflite")
        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)

        // 👇 Log input/output shapes
        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()
        Log.d("TFLite", "Input shape: ${inputShape.contentToString()}")
        Log.d("TFLite", "Output shape: ${outputShape.contentToString()}")
    }

    /**
     * Load the .tflite model file from assets
     */
    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        FileInputStream(assetFileDescriptor.fileDescriptor).use { input ->
            val fileChannel = input.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    /**
     * Enhance the given bitmap using the ESRGAN model
     */
    fun enhance(bitmap: Bitmap): Bitmap {
        val inputHeight = 180
        val inputWidth = 320
        val outputHeight = 720
        val outputWidth = 1280

        // 1️⃣ Resize input image to model input size
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)

        // 2️⃣ Prepare input array [1, H, W, 3] normalized to [0, 1]
        val input = Array(1) { Array(inputHeight) { Array(inputWidth) { FloatArray(3) } } }
        for (y in 0 until inputHeight) {
            for (x in 0 until inputWidth) {
                val pixel = resizedBitmap.getPixel(x, y)
                input[0][y][x][0] = ((pixel shr 16 and 0xFF) / 255f) // R
                input[0][y][x][1] = ((pixel shr 8 and 0xFF) / 255f)  // G
                input[0][y][x][2] = ((pixel and 0xFF) / 255f)        // B
            }
        }

        // 3️⃣ Prepare output buffer [1, 720, 1280, 3]
        val output = Array(1) { Array(outputHeight) { Array(outputWidth) { FloatArray(3) } } }

        // 4️⃣ Run the model
        interpreter.run(input, output)

        // 5️⃣ Convert model output → Bitmap
        val enhancedBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        for (y in 0 until outputHeight) {
            for (x in 0 until outputWidth) {
                val r = (output[0][y][x][0] * 255f).toInt().coerceIn(0, 255)
                val g = (output[0][y][x][1] * 255f).toInt().coerceIn(0, 255)
                val b = (output[0][y][x][2] * 255f).toInt().coerceIn(0, 255)
                enhancedBitmap.setPixel(x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
            }
        }

        return enhancedBitmap
    }




    fun close() {
        try {
            interpreter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
