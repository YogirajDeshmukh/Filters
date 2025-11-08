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
import com.example.filter.domain.BackgroundOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.media.MediaScannerConnection

/**
 * Background Removal Screen using ML Kit Selfie Segmentation.
 * Allows users to pick an image, select a background style, remove background, and save result.
 */
@Composable
fun BackgroundRemovalScreen() {
    val context = LocalContext.current

    // ✅ ViewModel setup
    val viewModel: BackgroundRemovalViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BackgroundRemovalViewModel(context) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // ✅ Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = getRealPathFromUri(context, it)
            selectedImagePath = path
            selectedBitmap = BitmapFactory.decodeFile(path)
            selectedBitmap?.let { bmp -> viewModel.selectOriginal(bmp) }
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
            Text("Background Removal", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Select Image
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Select Image from Gallery")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option Selector
            BackgroundOptionSelector(
                currentOption = uiState.option,
                onOptionChange = { viewModel.changeOption(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Remove Background button
            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.Default) {
                        viewModel.removeBackground()
                    }
                },
                enabled = !uiState.isLoading && uiState.original != null
            ) {
                Text(if (uiState.isLoading) "Processing..." else "Remove Background")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Preview Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                uiState.original?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Original")
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Original",
                            modifier = Modifier.size(150.dp)
                        )
                    }
                }

                uiState.result?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Result")
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Result",
                            modifier = Modifier.size(150.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            uiState.result?.let { bitmap ->
                Button(
                    onClick = {
                        val savedPath = saveImageToGallery(context, bitmap)
                        Toast.makeText(
                            context,
                            "✅ Saved to Gallery!\n📁 $savedPath",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text("Save Result Image")
                }
            }
        }
    }
}

/**
 * Selector for choosing background style (Transparent / Solid / Blur)
 */
@Composable
fun BackgroundOptionSelector(currentOption: BackgroundOption, onOptionChange: (BackgroundOption) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select Background Type:")
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            BackgroundOption.entries.forEach { option ->
                val selected = option == currentOption
                FilterChip(
                    selected = selected,
                    onClick = { onOptionChange(option) },
                    label = { Text(option.name) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

/**
 * Utility: Converts URI to a local file path by saving a temp copy
 */
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

/**
 * Utility: Saves the result image to the Pictures/FilterApp folder.
 */
private fun saveImageToGallery(context: android.content.Context, bitmap: Bitmap): String {
    val filename = "bg_removed_${System.currentTimeMillis()}.png"
    val picturesDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "FilterApp"
    )
    if (!picturesDir.exists()) picturesDir.mkdirs()

    val file = File(picturesDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
    }

    MediaScannerConnection.scanFile(
        context,
        arrayOf(file.absolutePath),
        arrayOf("image/png"),
        null
    )

    return file.absolutePath
}
