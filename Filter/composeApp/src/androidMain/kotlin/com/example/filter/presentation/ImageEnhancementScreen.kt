package com.example.filter.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filter.ImageProcessorImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.media.MediaScannerConnection


//Allows users to pick an image, enhance it, preview, and save the result.

@Composable
fun ImageEnhancementScreen() {
    val context = LocalContext.current

    // ViewModel
    val viewModel: ImageEnhancementViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ImageEnhancementViewModel(context) as T
            }
        }
    )

    val enhancedBitmap by viewModel.enhancedImage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val imageProcessor = remember { ImageProcessorImpl(context) }

    // Gallery picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = getRealPathFromUri(context, it)
            selectedImagePath = path
            selectedBitmap = BitmapFactory.decodeFile(path)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Image Enhancement", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Select image button
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Select Image from Gallery")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enhance button
            selectedImagePath?.let { path ->
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.Default) {
                            viewModel.enhance(path)
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Enhancing..." else "Enhance Image")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display images side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                selectedBitmap?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Original")
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Original Image",
                            modifier = Modifier.size(150.dp)
                        )
                    }
                }

                enhancedBitmap?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Enhanced")
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Enhanced Image",
                            modifier = Modifier.size(150.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            enhancedBitmap?.let { bitmap ->
                Button(
                    onClick = {
                        val savedPath = saveEnhancedImageToGallery(context, bitmap)
                        Toast.makeText(
                            context,
                            "Saved to Gallery!\n $savedPath",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    enabled = !isLoading
                ) {
                    Text("Save Enhanced Image")
                }
            }
        }
    }
}

// URI to Bitmap and save temporarily
private fun getRealPathFromUri(context: android.content.Context, uri: Uri): String? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }

        val file = File(context.cacheDir, "selected_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
            it.flush()
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//Save final enhanced image to gallery
private fun saveEnhancedImageToGallery(context: android.content.Context, bitmap: Bitmap): String {
    val filename = "enhanced_${System.currentTimeMillis()}.jpg"
    val picturesDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "FilterApp"
    )
    if (!picturesDir.exists()) picturesDir.mkdirs()

    val file = File(picturesDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        out.flush()
    }

    MediaScannerConnection.scanFile(
        context,
        arrayOf(file.absolutePath),
        arrayOf("image/jpeg"),
        null
    )

    return file.absolutePath
}
