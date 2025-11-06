package com.example.filter.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.filter.model.ESRGANModelInterpreter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual class ImageEnhancer actual constructor() {

    private var model: ESRGANModelInterpreter? = null
    private var appContext: Context? = null

    fun attachContext(context: Context) { appContext = context }

    actual fun initModel() {
        requireNotNull(appContext) { "Context not attached. Call attachContext() first." }
        model = ESRGANModelInterpreter(appContext!!)
    }

    actual fun enhance(imageData: ByteArray): ByteArray {
        val bmp = BitmapFactory.decodeStream(ByteArrayInputStream(imageData))
        val enhanced = model?.enhanceImage(bmp) ?: bmp
        val output = ByteArrayOutputStream()
        enhanced.compress(Bitmap.CompressFormat.JPEG, 95, output)
        return output.toByteArray()
    }

    actual fun close() {
        model?.close()
    }

    actual fun attachContext(context: Any) {
    }
}
