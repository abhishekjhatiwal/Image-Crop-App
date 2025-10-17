package com.example.imagecrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.imagecrop.screen.ImageCropper
import com.example.imagecrop.ui.theme.ImageCropTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageCropTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageCropper()
                }
            }
        }
    }
}
