// app/src/main/java/com/example/ecodeliandroid/MainActivity.kt
package com.example.ecodeliandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecodeliandroid.ui.theme.EcoDeliAndroidTheme
import com.example.ecodeliandroid.repository.AuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoDeliAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EcoDeliApp()
                }
            }
        }
    }
}

@Composable
fun EcoDeliApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }

    // Déterminer l'écran de départ selon l'état de connexion
    val startDestination = if (authRepository.isLoggedIn()) "main" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Écran de connexion
        composable("login") {
            LoginScreen(navController)
        }

        // Écran principal avec navigation par onglets
        composable("main") {
            MainScreen(navController)
        }

        // Détail d'une livraison
        composable("delivery_detail/{deliveryId}") { backStackEntry ->
            val deliveryId = backStackEntry.arguments?.getString("deliveryId") ?: ""
            DeliveryDetailScreen(navController, deliveryId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavController) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "EcoDeli",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val tabs = listOf(
                    BottomNavItem("Livraisons", Icons.Filled.LocalShipping, "deliveries"),
                    BottomNavItem("Prestations", Icons.Filled.Build, "services"),
                    BottomNavItem("Profil", Icons.Filled.Person, "profile")
                )

                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "deliveries",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("deliveries") {
                DeliveriesScreen(rootNavController) // Passer rootNavController pour navigation vers détails
            }
            composable("services") {
                ServicesScreen(rootNavController)
            }
            composable("profile") {
                ProfileScreen(rootNavController) // Passer rootNavController pour navigation vers login
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)