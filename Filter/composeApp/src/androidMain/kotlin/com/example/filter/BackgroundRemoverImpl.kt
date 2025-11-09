package com.example.filter.data

import android.content.Context
import android.graphics.*
import com.example.filter.domain.BackgroundOption
import com.example.filter.domain.BackgroundRemovalUseCase
import com.example.filter.domain.ImageData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.tasks.await

//  implementation of background removal impl using ML Kit Selfie

class BackgroundRemoverImpl(
    private val context: Context
) : BackgroundRemovalUseCase(
    remover = { imageData, option ->
        val safeBitmap = ensureMutableBitmap(imageData.bitmap)
        val resultBitmap = removeBackground(safeBitmap, option, context)
        ImageData(resultBitmap) // wrap back into shared ImageData for domain
    }
) {
    companion object {


         // Ensure the input bitmap is mutable and uses ARGB_8888 (not Config.HARDWARE)

        private fun ensureMutableBitmap(bitmap: Bitmap): Bitmap {
            return if (bitmap.config != Bitmap.Config.ARGB_8888 || !bitmap.isMutable) {
                bitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else bitmap
        }


//        Performs ML Kit segmentation to separate foreground & background,
//        then merges depending on [BackgroundOption].

        suspend fun removeBackground(
            inputBitmap: Bitmap,
            option: BackgroundOption,
            context: Context
        ): Bitmap {
            // Prepare safe bitmap and ML Kit input
            val safeBitmap = ensureMutableBitmap(inputBitmap)

            val image = InputImage.fromBitmap(safeBitmap, 0)
            val segmenterOptions = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .build()
            val segmenter = Segmentation.getClient(segmenterOptions)

            // Run segmentation
            val result = segmenter.process(image).await()
            val maskBuffer = result.buffer
            val width = result.width
            val height = result.height

            // Extract float alpha mask
            val alpha = FloatArray(width * height)
            maskBuffer.asFloatBuffer().get(alpha)

            //  Create readable scaled bitmap
            val scaled = Bitmap.createScaledBitmap(safeBitmap, width, height, true)
                .copy(Bitmap.Config.ARGB_8888, true) // ensure mutable, CPU-accessible

            // Build transparent foreground
            val pixels = IntArray(width * height)
            scaled.getPixels(pixels, 0, width, 0, 0, width, height)

            for (i in pixels.indices) {
                val a = (alpha[i] * 255).toInt().coerceIn(0, 255)
                pixels[i] = (a shl 24) or (pixels[i] and 0x00FFFFFF)
            }

            val foreground = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

            //  Prepare background
            val background: Bitmap = when (option) {
                BackgroundOption.TRANSPARENT -> {
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(Color.TRANSPARENT)
                    }
                }

                BackgroundOption.SOLID_COLOR -> {
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(Color.WHITE) // You can change this color
                    }
                }

            }

            //  Composite both layers
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            canvas.drawBitmap(background, 0f, 0f, null)
            canvas.drawBitmap(foreground, 0f, 0f, null)

            return output
        }


        //not giving proper results

//          Simple blur effect using RenderScript alternative (Canvas-based).
//         For strong blur, replace with GPU-based library or RenderEffect .
//
//        private fun applyBlur(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
//            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//            val canvas = Canvas(output)
//            val paint = Paint().apply {
//                isAntiAlias = true
//                maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
//            }
//            canvas.drawBitmap(bitmap, 0f, 0f, paint)
//            return output
//        }



    }
}
