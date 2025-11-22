package com.example.comic_Market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TableScreen(onNavigateToMap: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.LightGray), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Table Screen", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onNavigateToMap) {
            Text("地図へ")
        }
    }
}

