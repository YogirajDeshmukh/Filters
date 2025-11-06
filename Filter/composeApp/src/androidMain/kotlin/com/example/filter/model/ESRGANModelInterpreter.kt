package com.example.filter.model

import android.content.Context
import android.graphics.Bitmap
import com.example.filter.utils.BitmapUtils
import com.example.filter.utils.ImagePreprocessor
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.util.Log

class ESRGANModelInterpreter(private val context: Context) {

    private lateinit var interpreter: Interpreter

    init {
        Log.d("ImageEnhancer", "Initializing ESRGAN model...")
        loadModel("RealESRGAN_x4plus_float.tflite")
    }

    private fun loadModel(modelName: String) {
        val buffer = loadModelFileSafe(modelName)
        val options = Interpreter.Options().apply {
            setUseXNNPACK(true)
            setNumThreads(4)
        }

        interpreter = Interpreter(buffer, options)
        Log.d("ImageEnhancer", "✅ ESRGAN model loaded and interpreter initialized.")
    }

    // 🔒 Safe loader: works even if .tflite is compressed in AAB
    private fun loadModelFileSafe(modelName: String): ByteBuffer {
        val assetManager = context.assets
        assetManager.open(modelName).use { input ->
            val bytes = input.readBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.order(ByteOrder.nativeOrder())
            buffer.put(bytes)
            buffer.rewind()
            Log.d("ImageEnhancer", "✅ Model loaded into memory (${bytes.size} bytes)")
            return buffer
        }
    }

    fun enhanceImage(bitmap: Bitmap): Bitmap {
        val origWidth = bitmap.width
        val origHeight = bitmap.height

        // 🔹 Preprocess: pad to square and resize to model input
        val inputBuffer = ImagePreprocessor.preprocess(bitmap)
        val outputShape = intArrayOf(1, 512, 512, 3)
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, org.tensorflow.lite.DataType.FLOAT32)

        Log.d("ImageEnhancer", "Running model inference...")
        interpreter.run(inputBuffer, outputBuffer.buffer.rewind())

        // 🔹 Convert output tensor to Bitmap
        val enhanced = BitmapUtils.convertTensorToBitmap(outputBuffer, 512, 512)

        // 🔹 Crop back to original aspect ratio
        val finalBitmap = cropToOriginalAspect(enhanced, origWidth, origHeight)
        Log.d("ImageEnhancer", "✅ Image enhanced successfully (output: ${finalBitmap.width}x${finalBitmap.height})")

        return finalBitmap
    }

    // 🔹 Maintain original aspect ratio by cropping excess parts
    private fun cropToOriginalAspect(enhanced: Bitmap, origWidth: Int, origHeight: Int): Bitmap {
        val targetRatio = origWidth.toFloat() / origHeight.toFloat()
        val width = enhanced.width
        val height = enhanced.height
        val currentRatio = width.toFloat() / height.toFloat()

        var cropWidth = width
        var cropHeight = height

        if (currentRatio > targetRatio) {
            // Image too wide → crop sides
            cropWidth = (height * targetRatio).toInt()
        } else if (currentRatio < targetRatio) {
            // Image too tall → crop top/bottom
            cropHeight = (width / targetRatio).toInt()
        }

        val xOffset = (width - cropWidth) / 2
        val yOffset = (height - cropHeight) / 2

        return Bitmap.createBitmap(enhanced, xOffset, yOffset, cropWidth, cropHeight)
    }

    fun close() {
        if (::interpreter.isInitialized) interpreter.close()
    }
}
