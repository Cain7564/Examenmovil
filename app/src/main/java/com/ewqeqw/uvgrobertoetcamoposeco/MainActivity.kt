package com.ewqeqw.uvgrobertoetcamoposeco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ewqeqw.uvgrobertoetcamoposeco.ui.events.AssetDetailEvent
import com.ewqeqw.uvgrobertoetcamoposeco.ui.screens.AssetDetailScreen
import com.ewqeqw.uvgrobertoetcamoposeco.ui.screens.AssetListScreen
import com.ewqeqw.uvgrobertoetcamoposeco.ui.theme.UvgRobertoETCamoposecoTheme
import com.ewqeqw.uvgrobertoetcamoposeco.ui.viewmodel.AssetDetailViewModel
import com.ewqeqw.uvgrobertoetcamoposeco.ui.viewmodel.AssetListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UvgRobertoETCamoposecoTheme {
                CryptoNavigation()
            }
        }
    }
}

@Composable
fun CryptoNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            val viewModel: AssetListViewModel = viewModel()
            AssetListScreen(
                viewModel = viewModel,
                onNavigateToDetail = { assetId ->
                    navController.navigate("detail/$assetId")
                }
            )
        }

        composable("detail/{id}") { backStackEntry ->
            val assetId = backStackEntry.arguments?.getString("id")!!
            val viewModel: AssetDetailViewModel = viewModel()

            LaunchedEffect(assetId) {
                viewModel.onEvent(AssetDetailEvent.LoadAssetDetail(assetId))
            }

            AssetDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}