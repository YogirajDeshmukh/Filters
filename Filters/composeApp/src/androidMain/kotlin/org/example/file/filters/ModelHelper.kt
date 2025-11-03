package org.example.file.filters

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

object ModelHelper {

    // Load the ESRGAN.tflite model from assets
    fun loadModelFile(context: Context, modelName: String = "ESRGAN.tflite"): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Create and return a TensorFlow Lite Interpreter
    fun getInterpreter(context: Context): Interpreter {
        val modelBuffer = loadModelFile(context)
        return Interpreter(modelBuffer)
    }
}
