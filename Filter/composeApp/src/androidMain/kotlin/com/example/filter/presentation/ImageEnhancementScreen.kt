package com.example.filter.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream

@Composable
fun ImageEnhancementScreen() {
    val context = LocalContext.current
    val viewModel = remember { ImageEnhancementViewModel(context) }
    val enhancedBytes by viewModel.enhancedImage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            originalBitmap = bmp
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("AI Image Enhancement",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
                                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),

                )

            Button(onClick = { launcher.launch("image/*") }) {
                Text("Pick Image")
            }

            originalBitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "Original", modifier = Modifier.fillMaxWidth().height(200.dp))
            }

            enhancedBytes?.let {
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "Enhanced", modifier = Modifier.fillMaxWidth().height(200.dp))
            }

            Button(onClick = {
                originalBitmap?.let {
                    val output = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.JPEG, 100, output)
                    viewModel.enhance(output.toByteArray())
                }
            }, enabled = originalBitmap != null && !isLoading) {
                Text("Enhance Image")
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(10.dp))
                    Text("Enhancing image...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
