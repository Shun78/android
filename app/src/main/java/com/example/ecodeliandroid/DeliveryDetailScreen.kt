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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailScreen(
    navController: NavController,
    deliveryId: String
) {
    // Données mockées - normalement récupérées via l'API
    val delivery = remember {
        when (deliveryId) {
            "1" -> Delivery(
                id = "1",
                title = "Colis fragile - Radiateur",
                status = DeliveryStatus.EN_COURS,
                from = "Paris 19ème",
                to = "Marseille Centre",
                date = "15 Jan 2025",
                price = "62€"
            )
            "3" -> Delivery(
                id = "3",
                title = "Vélo électrique",
                status = DeliveryStatus.LIVREE,
                from = "Nice Centre",
                to = "Gennevilliers",
                date = "12 Jan 2025",
                price = "144€",
                deliveryCode = "BXYZ789"
            )
            else -> Delivery(
                id = "2",
                title = "Étagères IKEA",
                status = DeliveryStatus.EN_ATTENTE,
                from = "Lyon Part-Dieu",
                to = "Montrouge",
                date = "18 Jan 2025",
                price = "19€"
            )
        }
    }

    var showValidationDialog by remember { mutableStateOf(false) }
    var validationCode by remember { mutableStateOf("") }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // En-tête avec statut
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
                        DeliveryStatusChip(status = delivery.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = delivery.price,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Informations de transport
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

                    DetailInfoRow(
                        icon = Icons.Filled.LocationOn,
                        label = "Départ",
                        value = delivery.from,
                        iconColor = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailInfoRow(
                        icon = Icons.Filled.Flag,
                        label = "Arrivée",
                        value = delivery.to,
                        iconColor = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailInfoRow(
                        icon = Icons.Filled.DateRange,
                        label = "Date",
                        value = delivery.date,
                        iconColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Code de livraison si disponible
            if (delivery.deliveryCode != null) {
                Spacer(modifier = Modifier.height(20.dp))

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
                            text = "Code de livraison",
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
                                text = delivery.deliveryCode,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Communiquez ce code au livreur",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton d'action selon le statut
            when (delivery.status) {
                DeliveryStatus.EN_COURS -> {
                    Button(
                        onClick = { showValidationDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Valider la livraison", fontSize = 16.sp)
                    }
                }
                DeliveryStatus.LIVREE -> {
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
                DeliveryStatus.EN_ATTENTE -> {
                    OutlinedButton(
                        onClick = { /* Action pour annuler */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Annuler la livraison", fontSize = 16.sp)
                    }
                }
                DeliveryStatus.ANNULEE -> {
                    // Pas de bouton pour les livraisons annulées
                }
            }
        }
    }

    // Dialog de validation
    if (showValidationDialog) {
        AlertDialog(
            onDismissRequest = { showValidationDialog = false },
            title = { Text("Valider la livraison") },
            text = {
                Column {
                    Text("Entrez le code de validation fourni par le livreur :")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = validationCode,
                        onValueChange = { validationCode = it },
                        label = { Text("Code de validation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Ici, l'appel API sera fait par votre collègue
                        showValidationDialog = false
                        validationCode = ""
                    }
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showValidationDialog = false
                        validationCode = ""
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
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