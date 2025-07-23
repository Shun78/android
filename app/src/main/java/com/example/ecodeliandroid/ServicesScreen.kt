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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ServicesScreen(navController: NavController) {
    // Données mockées
    val services = remember {
        listOf(
            Service(
                id = "1",
                title = "Transport personne âgée",
                status = ServiceStatus.CONFIRMEE,
                date = "20 Jan 2025 - 14h30",
                price = "25€",
                description = "Transport chez le médecin - Dr. Martin, 15 rue de la Paix"
            ),
            Service(
                id = "2",
                title = "Courses alimentaires",
                status = ServiceStatus.EN_COURS,
                date = "16 Jan 2025 - 10h00",
                price = "18€",
                description = "Courses au Monoprix - Liste fournie"
            ),
            Service(
                id = "3",
                title = "Garde d'animaux",
                status = ServiceStatus.TERMINEE,
                date = "14 Jan 2025 - 16h00",
                price = "35€",
                description = "Garde de Minou (chat) pendant transport"
            ),
            Service(
                id = "4",
                title = "Achat produits Angleterre",
                status = ServiceStatus.CONFIRMEE,
                date = "25 Jan 2025 - 12h00",
                price = "45€",
                description = "Jelly anglaise - Marks & Spencer"
            ),
            Service(
                id = "5",
                title = "Transfert aéroport",
                status = ServiceStatus.TERMINEE,
                date = "12 Jan 2025 - 06h30",
                price = "60€",
                description = "CDG Terminal 2E vers centre-ville"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mes Prestations",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services) { service ->
                ServiceCard(
                    service = service,
                    onClick = {
                        // Navigation vers détail de prestation si nécessaire
                    }
                )
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: Service,
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
                        text = service.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = service.date,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    ServiceStatusChip(status = service.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = service.price,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Description",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = service.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Actions selon le statut
            when (service.status) {
                ServiceStatus.CONFIRMEE -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Modifier la prestation */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifier", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { /* Annuler la prestation */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Annuler", fontSize = 12.sp)
                        }
                    }
                }
                ServiceStatus.TERMINEE -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { /* Noter la prestation */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Noter la prestation", fontSize = 14.sp)
                    }
                }
                else -> {
                    // Pas d'actions pour EN_COURS et ANNULEE
                }
            }
        }
    }
}

@Composable
fun ServiceStatusChip(status: ServiceStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        ServiceStatus.CONFIRMEE -> Triple(
            Color(0xFFE8F5E8),
            Color(0xFF2E7D32),
            "Confirmée"
        )
        ServiceStatus.EN_COURS -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "En cours"
        )
        ServiceStatus.TERMINEE -> Triple(
            Color(0xFFF3E5F5),
            Color(0xFF7B1FA2),
            "Terminée"
        )
        ServiceStatus.ANNULEE -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            "Annulée"
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