package com.example.filter.presentation

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filter.domain.BackgroundOption
import com.example.filter.data.BackgroundRemoverImpl
import com.example.filter.domain.BackgroundRemovalUseCase
import com.example.filter.domain.ImageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RemovalUiState(
    val isLoading: Boolean = false,
    val original: Bitmap? = null,
    val result: Bitmap? = null,
    val option: BackgroundOption = BackgroundOption.TRANSPARENT
)

class BackgroundRemovalViewModel(context: Context) : ViewModel() {

    private val useCase: BackgroundRemovalUseCase = BackgroundRemoverImpl(context)

    private val _uiState = MutableStateFlow(RemovalUiState())
    val uiState = _uiState.asStateFlow()

    fun selectOriginal(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(original = bitmap, result = null)
    }

    fun changeOption(option: BackgroundOption) {
        _uiState.value = _uiState.value.copy(option = option)
    }

    fun removeBackground() {
        val originalBitmap = _uiState.value.original ?: return
        val option = _uiState.value.option

        viewModelScope.launch(Dispatchers.Default) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // ✅ Wrap Bitmap into ImageData before sending to domain
                val resultImageData = useCase.execute(ImageData(originalBitmap), option)

                // ✅ Extract Bitmap from ImageData to update UI
                val resultBitmap = resultImageData.bitmap

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    result = resultBitmap
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
