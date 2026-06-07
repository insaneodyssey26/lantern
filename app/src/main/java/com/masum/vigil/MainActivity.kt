package com.masum.vigil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.masum.vigil.navigation.VigilNavHost
import com.masum.vigil.ui.theme.VigilTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VigilAppRoot()
        }
    }
}

@Composable
private fun VigilAppRoot() {
    VigilTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            VigilNavHost(modifier = Modifier.padding(innerPadding))
        }
    }
}
