package com.foodwaste.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foodwaste.app.ui.inventory.InventoryScreen
import com.foodwaste.app.ui.inventory.InventoryViewModel
import com.foodwaste.app.ui.recipes.RecipesScreen
import com.foodwaste.app.ui.recipes.RecipesViewModel
import com.foodwaste.app.ui.scan.ScanScreen
import com.foodwaste.app.ui.scan.ScanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as FoodWasteApplication
        setContent {
            MaterialTheme { Root(app) }
        }
    }
}

private object Routes {
    const val INVENTORY = "inventory"
    const val SCAN = "scan"
    const val RECIPES = "recipes"
}

@Composable
private fun Root(app: FoodWasteApplication) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Routes.INVENTORY,
                    onClick = { nav.navigate(Routes.INVENTORY) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Kitchen, null) },
                    label = { Text("Inventory") }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.SCAN,
                    onClick = { nav.navigate(Routes.SCAN) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.CameraAlt, null) },
                    label = { Text("Scan") }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.RECIPES,
                    onClick = { nav.navigate(Routes.RECIPES) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Restaurant, null) },
                    label = { Text("Recipes") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.INVENTORY,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.INVENTORY) {
                val vm: InventoryViewModel = viewModel(factory = factory { InventoryViewModel(app.repo) })
                InventoryScreen(vm)
            }
            composable(Routes.SCAN) {
                val vm: ScanViewModel = viewModel(factory = factory { ScanViewModel(app.parser, app.repo) })
                ScanScreen(vm, onSaved = { nav.navigate(Routes.INVENTORY) })
            }
            composable(Routes.RECIPES) {
                val vm: RecipesViewModel = viewModel(factory = factory { RecipesViewModel(app.repo) })
                RecipesScreen(vm)
            }
        }
    }
}

/** Tiny inline ViewModelProvider.Factory helper so we don't pull in Hilt in v0. */
private inline fun <reified T : ViewModel> factory(crossinline create: () -> T) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
