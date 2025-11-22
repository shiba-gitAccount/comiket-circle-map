package com.example.comic_Market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comic_Market.ui.theme.ComikeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComikeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComicMarketApp(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComicMarketApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "map") {
        composable("map") {
            MapScreen(
                onNavigateToTable = { navController.navigate("table") }
            )
        }
        composable("table") {
            TableScreen(
                onNavigateToMap = { navController.navigate("map") }
            )
        }
    }
}