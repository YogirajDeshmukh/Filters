package com.example.filter.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.DataType

object ImagePreprocessor {

    fun preprocess(bitmap: Bitmap, inputSize: Int = 128): TensorImage {
        // Resize image to model input size
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        // Convert Bitmap to TensorImage (float32)
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(resizedBitmap)

        // Normalize pixel values: 0–255 → 0–1
        val buffer = tensorImage.buffer
        buffer.rewind()
        return tensorImage
    }
}
