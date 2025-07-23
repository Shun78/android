// app/src/main/java/com/example/ecodeliandroid/DeliveryDetailScreen.kt
package com.example.ecodeliandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.ecodeliandroid.repository.EcoDeliRepository
import com.example.ecodeliandroid.network.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(
    navController: NavController,
    deliveryId: String
) {
    val context = LocalContext.current
    val repository = remember { EcoDeliRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // États pour l'interface
    var delivery by remember { mutableStateOf<Task?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // États pour les actions
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationCode by remember { mutableStateOf("") }
    var isValidating by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Charger les détails de la livraison
    LaunchedEffect(deliveryId) {
        isLoading = true
        error = null

        val result = repository.getTaskDetails(deliveryId)
        result.fold(
            onSuccess = { task ->
                delivery = task
                isLoading = false
            },
            onFailure = { exception ->
                error = "Erreur lors du chargement: ${exception.message}"
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail de la livraison") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Erreur",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Retour")
                            }
                        }
                    }
                }
            }
            delivery != null -> {
                DeliveryDetailContent(
                    delivery = delivery!!,
                    onValidateDelivery = { showValidationDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Dialog de validation
    if (showValidationDialog) {
        AlertDialog(
            onDismissRequest = {
                showValidationDialog = false
                validationCode = ""
                validationError = null
            },
            title = { Text("Valider la livraison") },
            text = {
                Column {
                    Text("Entrez le code de validation fourni par le client :")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = validationCode,
                        onValueChange = { validationCode = it },
                        label = { Text("Code de validation") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = validationError != null
                    )
                    validationError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            isValidating = true
                            validationError = null

                            val result = repository.validateTaskCompletion(deliveryId, validationCode)
                            result.fold(
                                onSuccess = { updatedTask ->
                                    delivery = updatedTask
                                    showValidationDialog = false
                                    validationCode = ""
                                    // Optionnellement, marquer les messages comme lus
                                    repository.markMessagesAsRead(deliveryId)
                                },
                                onFailure = { exception ->
                                    validationError = exception.message ?: "Code de validation incorrect"
                                }
                            )
                            isValidating = false
                        }
                    },
                    enabled = validationCode.isNotBlank() && !isValidating
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(
                            size = 16.dp,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Valider")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showValidationDialog = false
                        validationCode = ""
                        validationError = null
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun DeliveryDetailContent(
    delivery: Task,
    onValidateDelivery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // En-tête avec statut
        DeliveryHeaderCard(delivery = delivery)

        Spacer(modifier = Modifier.height(20.dp))

        // Informations de transport
        DeliveryTransportCard(delivery = delivery)

        // Code de validation si disponible
        delivery.applications?.find {
            it.status == ApplicationStatus.COMPLETED || it.status == ApplicationStatus.VALIDATED
        }?.validationCode?.let { code ->
            Spacer(modifier = Modifier.height(20.dp))
            ValidationCodeCard(validationCode = code)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton d'action selon le statut
        ActionButton(
            delivery = delivery,
            onValidateDelivery = onValidateDelivery
        )
    }
}

@Composable
fun DeliveryHeaderCard(delivery: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = delivery.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Livraison #${delivery.id}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TaskStatusChip(status = delivery.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = formatPrice(delivery.calculatedPriceInCents),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Informations client
            delivery.user.let { user ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Client: ${user.firstName} ${user.lastName}".trim(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DeliveryTransportCard(delivery: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Informations de transport",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            delivery.shipping?.let { shipping ->
                DetailInfoRow(
                    icon = Icons.Filled.LocationOn,
                    label = "Départ",
                    value = shipping.pickupAddress.fullAddress,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailInfoRow(
                    icon = Icons.Filled.Flag,
                    label = "Arrivée",
                    value = shipping.deliveryAddress.fullAddress,
                    iconColor = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailInfoRow(
                    icon = Icons.Filled.Category,
                    label = "Catégorie",
                    value = getPackageCategoryText(shipping.packageCategory),
                    iconColor = MaterialTheme.colorScheme.tertiary
                )

                // Distance et durée si disponibles
                shipping.estimatedDistanceInMeters?.let { distance ->
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailInfoRow(
                        icon = Icons.Filled.Straighten,
                        label = "Distance",
                        value = formatDistance(distance),
                        iconColor = MaterialTheme.colorScheme.outline
                    )
                }

                shipping.estimatedDurationInMinutes?.let { duration ->
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailInfoRow(
                        icon = Icons.Filled.Schedule,
                        label = "Durée estimée",
                        value = formatDuration(duration),
                        iconColor = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                icon = Icons.Filled.DateRange,
                label = "Date de création",
                value = formatDate(delivery.createdAt),
                iconColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun ValidationCodeCard(validationCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Code de validation",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text(
                    text = validationCode,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Saisissez ce code pour valider la réception",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActionButton(
    delivery: Task,
    onValidateDelivery: () -> Unit
) {
    // Déterminer l'action possible selon le statut et les candidatures
    val myApplication = delivery.applications?.find {
        // Ici vous devriez vérifier si c'est la candidature de l'utilisateur connecté
        // Pour cet exemple, on prend la première candidature acceptée
        it.status == ApplicationStatus.ACCEPTED ||
                it.status == ApplicationStatus.COMPLETED ||
                it.status == ApplicationStatus.IN_PROGRESS
    }

    when {
        delivery.status == TaskStatus.COMPLETED && myApplication?.status == ApplicationStatus.COMPLETED -> {
            Button(
                onClick = onValidateDelivery,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmer la réception", fontSize = 16.sp)
            }
        }
        delivery.status == TaskStatus.DONE -> {
            OutlinedButton(
                onClick = { /* Action pour noter/commenter */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Noter la livraison", fontSize = 16.sp)
            }
        }
        delivery.status == TaskStatus.PUBLISHED -> {
            Button(
                onClick = { /* Action pour postuler */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Postuler à cette livraison", fontSize = 16.sp)
            }
        }
        delivery.status == TaskStatus.CANCELLED -> {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                enabled = false
            ) {
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Livraison annulée", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Fonctions utilitaires
private fun getPackageCategoryText(category: PackageCategory): String {
    return when (category) {
        PackageCategory.SMALL -> "Petit colis (jusqu'à 5kg)"
        PackageCategory.MEDIUM -> "Colis moyen (jusqu'à 15kg)"
        PackageCategory.LARGE -> "Gros colis (jusqu'à 30kg)"
    }
}

private fun formatDistance(distanceInMeters: Double): String {
    return if (distanceInMeters >= 1000) {
        String.format("%.1f km", distanceInMeters / 1000)
    } else {
        String.format("%.0f m", distanceInMeters)
    }
}

private fun formatDuration(durationInMinutes: Double): String {
    val hours = (durationInMinutes / 60).toInt()
    val minutes = (durationInMinutes % 60).toInt()

    return when {
        hours > 0 -> "${hours}h ${minutes}min"
        else -> "${minutes}min"
    }
}