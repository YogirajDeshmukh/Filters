package com.example.filter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.filter.presentation.ImageEnhancementScreen
import com.example.filter.presentation.BackgroundRemovalScreen

/**
 * Main entry point for the Filter app.
 * Provides navigation between Image Enhancement and Background Removal features.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf("home") }

                when (currentScreen) {
                    "home" -> HomeScreen(
                        onEnhanceClick = { currentScreen = "enhance" },
                        onBackgroundRemoveClick = { currentScreen = "background" }
                    )

                    "enhance" -> FeatureWrapper(
                        title = "Image Enhancement",
                        onBack = { currentScreen = "home" }
                    ) {
                        ImageEnhancementScreen()
                    }

                    "background" -> FeatureWrapper(
                        title = "Background Removal",
                        onBack = { currentScreen = "home" }
                    ) {
                        BackgroundRemovalScreen()
                    }
                }
            }
        }
    }
}

/**
 * Simple Home Screen with feature navigation buttons.
 */
@Composable
fun HomeScreen(
    onEnhanceClick: () -> Unit,
    onBackgroundRemoveClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to Filter App", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onEnhanceClick, modifier = Modifier.fillMaxWidth()) {
                Text("Image Enhancement")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackgroundRemoveClick, modifier = Modifier.fillMaxWidth()) {
                Text("Background Removal")
            }
        }
    }
}

/**
 * Reusable layout wrapper for feature screens (adds title and back button).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureWrapper(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("⬅ Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
