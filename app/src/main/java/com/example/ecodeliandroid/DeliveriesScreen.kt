// app/src/main/java/com/example/ecodeliandroid/DeliveriesScreen.kt
package com.example.ecodeliandroid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecodeliandroid.repository.EcoDeliRepository
import com.example.ecodeliandroid.network.models.*

@Composable
fun DeliveriesScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { EcoDeliRepository(context) }
    val viewModel: DeliveriesViewModel = viewModel()

    // États pour l'interface
    var deliveries by remember { mutableStateOf<List<Task>>(emptyList()) }
    var myApplications by remember { mutableStateOf<List<TaskApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    // Charger les données au démarrage
    LaunchedEffect(Unit) {
        isLoading = true
        error = null

        try {
            // Charger les livraisons disponibles
            val tasksResult = repository.getTasks(mapOf("type" to "SHIPPING"))
            tasksResult.fold(
                onSuccess = { tasks ->
                    deliveries = tasks.filter { it.type == TaskType.SHIPPING }
                },
                onFailure = { exception ->
                    error = "Erreur lors du chargement des livraisons: ${exception.message}"
                }
            )

            // Charger mes candidatures
            val applicationsResult = repository.getMyApplications()
            applicationsResult.fold(
                onSuccess = { applications ->
                    myApplications = applications.filter {
                        it.task.type == TaskType.SHIPPING
                    }
                },
                onFailure = { exception ->
                    error = "Erreur lors du chargement des candidatures: ${exception.message}"
                }
            )
        } catch (e: Exception) {
            error = "Erreur réseau: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Livraisons",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Onglets
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Disponibles") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Mes candidatures") }
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Erreur",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = {
                                // Relancer le chargement
                                isLoading = true
                                error = null
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Réessayer")
                        }
                    }
                }
            }
            else -> {
                when (selectedTab) {
                    0 -> {
                        // Onglet des livraisons disponibles
                        if (deliveries.isEmpty()) {
                            EmptyStateMessage(
                                message = "Aucune livraison disponible pour le moment",
                                icon = Icons.Filled.LocalShipping
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(deliveries) { delivery ->
                                    DeliveryCard(
                                        delivery = delivery,
                                        onClick = {
                                            navController.navigate("delivery_detail/${delivery.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Onglet des mes candidatures
                        if (myApplications.isEmpty()) {
                            EmptyStateMessage(
                                message = "Vous n'avez postulé à aucune livraison",
                                icon = Icons.Filled.LocalShipping
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(myApplications) { application ->
                                    ApplicationCard(
                                        application = application,
                                        onClick = {
                                            navController.navigate("delivery_detail/${application.task.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryCard(
    delivery: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = delivery.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(delivery.createdAt),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    TaskStatusChip(status = delivery.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPrice(delivery.calculatedPriceInCents),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Informations de livraison
            delivery.shipping?.let { shipping ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Départ",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = shipping.pickupAddress.mainText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "Arrivée",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = shipping.deliveryAddress.mainText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Catégorie du colis
                Spacer(modifier = Modifier.height(8.dp))
                PackageCategoryChip(category = shipping.packageCategory)
            }
        }
    }
}

@Composable
fun ApplicationCard(
    application: TaskApplication,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Candidature du ${formatDate(application.createdAt)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    ApplicationStatusChip(status = application.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPrice(application.task.calculatedPriceInCents),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Informations de livraison
            application.task.shipping?.let { shipping ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Départ",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = shipping.pickupAddress.mainText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "Arrivée",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = shipping.deliveryAddress.mainText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Code de validation si disponible
            if (application.validationCode != null &&
                (application.status == ApplicationStatus.COMPLETED || application.status == ApplicationStatus.VALIDATED)) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCode,
                        contentDescription = "Code",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Code: ${application.validationCode}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

// ViewModel pour gérer l'état
class DeliveriesViewModel : androidx.lifecycle.ViewModel() {
    // Vous pouvez ajouter ici la logique de gestion d'état avec StateFlow/LiveData
    // pour une architecture plus robuste
}