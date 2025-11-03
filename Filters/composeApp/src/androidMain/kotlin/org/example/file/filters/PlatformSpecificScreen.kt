package org.example.file.filters

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformSpecificScreen() {
    ImageEnhancerScreen() // this is your Android composable using ESRGAN
}
