package com.example.ecodeliandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecodeliandroid.ui.theme.EcoDeliAndroidTheme

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoDeliApp() {
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
                DeliveriesScreen(navController)
            }
            composable("services") {
                ServicesScreen(navController)
            }
            composable("profile") {
                ProfileScreen(navController)
            }
            composable("delivery_detail/{deliveryId}") { backStackEntry ->
                val deliveryId = backStackEntry.arguments?.getString("deliveryId") ?: ""
                DeliveryDetailScreen(navController, deliveryId)
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// Modèles de données mockées
data class Delivery(
    val id: String,
    val title: String,
    val status: DeliveryStatus,
    val from: String,
    val to: String,
    val date: String,
    val price: String,
    val deliveryCode: String? = null
)

enum class DeliveryStatus {
    EN_COURS, LIVREE, EN_ATTENTE, ANNULEE
}

data class Service(
    val id: String,
    val title: String,
    val status: ServiceStatus,
    val date: String,
    val price: String,
    val description: String
)

enum class ServiceStatus {
    CONFIRMEE, EN_COURS, TERMINEE, ANNULEE
}

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val memberSince: String
)

@Preview(showBackground = true)
@Composable
fun EcoDeliAppPreview() {
    EcoDeliAndroidTheme {
        EcoDeliApp()
    }
}