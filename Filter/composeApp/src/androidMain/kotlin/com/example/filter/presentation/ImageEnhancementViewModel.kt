package com.example.filter.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filter.domain.ImageEnhancer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageEnhancementViewModel(context: Context) : ViewModel() {

    private val enhancer = ImageEnhancer().apply {
        attachContext(context)
        initModel()
    }

    private val _enhancedImage = MutableStateFlow<ByteArray?>(null)
    val enhancedImage = _enhancedImage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun enhance(imageBytes: ByteArray) {
        viewModelScope.launch(Dispatchers.Default) {
            _isLoading.value = true
            val result = enhancer.enhance(imageBytes)
            _enhancedImage.value = result
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        enhancer.close()
    }
}
