package com.example.comic_Market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comic_Market.ui.theme.ComikeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComikeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    ComicMarketAppContainer()
                }
            }
        }
    }
}


@Composable
fun ComicMarketAppContainer() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ComicMarketAppContent(navController = navController, drawerState = drawerState, scope = scope)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicMarketAppContent(navController: NavHostController, drawerState: DrawerState, scope: CoroutineScope) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ComicMarketDrawer(navController, drawerState, scope)
        }
    ) {
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "table",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("table") { TableScreen() }
                composable("map") { MapScreen(onNavigateToTable = { navController.navigate("table") }) }
            }
        }
    }
}

@Composable
fun ComicMarketDrawer(navController: NavHostController, drawerState: DrawerState, scope: CoroutineScope) {
    ModalDrawerSheet(
        modifier = Modifier.width(200.dp)
    ) {
        Column( modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            DrawerItem("Table Screen") {
                navController.navigate("table")
                scope.launch { drawerState.close() }
            }
            DrawerItem("Map Screen") {
                navController.navigate("map")
                scope.launch { drawerState.close() }
            }
        }
    }
}

@Composable
fun DrawerItem(text: String, onClick: () -> Unit) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    )
}