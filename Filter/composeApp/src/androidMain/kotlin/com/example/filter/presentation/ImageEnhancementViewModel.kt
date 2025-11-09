package com.example.filter.presentation


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filter.ImageProcessorImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageEnhancementViewModel(private val context: Context) : ViewModel() {

    private val imageProcessor = ImageProcessorImpl(context)

    private val _enhancedImage = MutableStateFlow<Bitmap?>(null)
    val enhancedImage = _enhancedImage.asStateFlow()

    private val _enhancedImagePath = MutableStateFlow<String?>(null)
    val enhancedImagePath = _enhancedImagePath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


   //Enhances the image located at [imagePath],\
    // saves the enhanced image, and updates the state with the new Bitmap + path.

    fun enhance(imagePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                // Run ESRGAN enhancement and get saved path
                val savedPath = imageProcessor.enhanceImage(imagePath)

                // Decode the saved enhanced image to Bitmap for UI display
                val enhancedBitmap = BitmapFactory.decodeFile(savedPath)

                _enhancedImage.value = enhancedBitmap
                _enhancedImagePath.value = savedPath
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
