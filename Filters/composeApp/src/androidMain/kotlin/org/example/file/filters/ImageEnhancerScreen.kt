package org.example.file.filters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImageEnhancerScreen() {
    val context = LocalContext.current
    val enhancer = remember { ImageEnhancer(context) }
    val scope = rememberCoroutineScope() // ✅ used for launching background work

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var enhancedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            selectedBitmap = BitmapFactory.decodeStream(stream)
            enhancedBitmap = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AI Image Enhancer", style = MaterialTheme.typography.headlineSmall)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                enhancedBitmap != null -> Image(
                    bitmap = enhancedBitmap!!.asImageBitmap(),
                    contentDescription = "Enhanced Image",
                    modifier = Modifier.fillMaxSize()
                )
                selectedBitmap != null -> Image(
                    bitmap = selectedBitmap!!.asImageBitmap(),
                    contentDescription = "Original Image",
                    modifier = Modifier.fillMaxSize()
                )
                else -> Text("No image selected")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Select Image")
            }

            Button(
                onClick = {
                    if (selectedBitmap != null) {
                        isLoading = true
                        enhancedBitmap = null

                        // ✅ Launch background processing properly
                        scope.launch {
                            val result = withContext(Dispatchers.Default) {
                                enhancer.enhance(selectedBitmap!!)
                            }
                            enhancedBitmap = result
                            isLoading = false
                        }
                    }
                },
                enabled = selectedBitmap != null && !isLoading
            ) {
                Text("Enhance")
            }
        }
    }
}
