// app/src/main/java/com/example/ecodeliandroid/DeliveriesScreen.kt
package com.example.ecodeliandroid

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                            EmptyStateMessage("Aucune livraison disponible pour le moment")
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
                            EmptyStateMessage("Vous n'avez postulé à aucune livraison")
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

@Composable
fun TaskStatusChip(status: TaskStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TaskStatus.PUBLISHED -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "Disponible"
        )
        TaskStatus.IN_PROGRESS -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "En cours"
        )
        TaskStatus.COMPLETED -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFF57C00),
            "Complétée"
        )
        TaskStatus.DONE -> Triple(
            Color(0xFFE8F5E8),
            Color(0xFF2E7D32),
            "Terminée"
        )
        TaskStatus.CANCELLED -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            "Annulée"
        )
        TaskStatus.DRAFT -> Triple(
            Color(0xFFF5F5F5),
            Color(0xFF757575),
            "Brouillon"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun ApplicationStatusChip(status: ApplicationStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        ApplicationStatus.PENDING -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFF57C00),
            "En attente"
        )
        ApplicationStatus.ACCEPTED -> Triple(
            Color(0xFFE8F5E8),
            Color(0xFF2E7D32),
            "Acceptée"
        )
        ApplicationStatus.IN_PROGRESS -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "En cours"
        )
        ApplicationStatus.COMPLETED -> Triple(
            Color(0xFFF3E5F5),
            Color(0xFF7B1FA2),
            "Complétée"
        )
        ApplicationStatus.VALIDATED -> Triple(
            Color(0xFFE8F5E8),
            Color(0xFF2E7D32),
            "Validée"
        )
        ApplicationStatus.REJECTED -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            "Refusée"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun PackageCategoryChip(category: PackageCategory) {
    val (backgroundColor, textColor, text, emoji) = when (category) {
        PackageCategory.SMALL -> Quadruple(
            Color(0xFFE8F5E8),
            Color(0xFF2E7D32),
            "Petit colis",
            "📦"
        )
        PackageCategory.MEDIUM -> Quadruple(
            Color(0xFFFFF3E0),
            Color(0xFFF57C00),
            "Colis moyen",
            "📦"
        )
        PackageCategory.LARGE -> Quadruple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            "Gros colis",
            "📦"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$emoji $text",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.LocalShipping,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Fonctions utilitaires
private fun formatDate(dateString: String): String {
    // Vous pouvez utiliser une bibliothèque comme java.time pour un formatage plus robuste
    return try {
        val instant = java.time.Instant.parse(dateString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString.substring(0, 10) // Fallback basique
    }
}

private fun formatPrice(priceInCents: Int?): String {
    return if (priceInCents != null) {
        val euros = priceInCents / 100.0
        String.format("%.2f€", euros)
    } else {
        "Prix à définir"
    }
}

// Classe utilitaire pour les quadruples
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// ViewModel pour gérer l'état
class DeliveriesViewModel : androidx.lifecycle.ViewModel() {
    // Vous pouvez ajouter ici la logique de gestion d'état avec StateFlow/LiveData
    // pour une architecture plus robuste
}