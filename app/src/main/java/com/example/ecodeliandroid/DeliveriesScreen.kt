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
fun DeliveriesScreen(navController: NavController) {
    // Données mockées
    val deliveries = remember {
        listOf(
            Delivery(
                id = "1",
                title = "Colis fragile - Radiateur",
                status = DeliveryStatus.EN_COURS,
                from = "Paris 19ème",
                to = "Marseille Centre",
                date = "15 Jan 2025",
                price = "62€"
            ),
            Delivery(
                id = "2",
                title = "Étagères IKEA",
                status = DeliveryStatus.EN_ATTENTE,
                from = "Lyon Part-Dieu",
                to = "Montrouge",
                date = "18 Jan 2025",
                price = "19€"
            ),
            Delivery(
                id = "3",
                title = "Vélo électrique",
                status = DeliveryStatus.LIVREE,
                from = "Nice Centre",
                to = "Gennevilliers",
                date = "12 Jan 2025",
                price = "144€",
                deliveryCode = "BXYZ789"
            ),
            Delivery(
                id = "4",
                title = "Cartons déménagement",
                status = DeliveryStatus.LIVREE,
                from = "Lussac-les-Châteaux",
                to = "Gallargues-le-Montueux",
                date = "10 Jan 2025",
                price = "50€",
                deliveryCode = "AXCD123"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mes Livraisons",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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

@Composable
fun DeliveryCard(
    delivery: Delivery,
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
                        text = delivery.date,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    DeliveryStatusChip(status = delivery.status)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = delivery.price,
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
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Départ",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = delivery.from,
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
                    text = delivery.to,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (delivery.deliveryCode != null) {
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
                        text = "Code: ${delivery.deliveryCode}",
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
fun DeliveryStatusChip(status: DeliveryStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        DeliveryStatus.EN_COURS -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "En cours"
        )
        DeliveryStatus.LIVREE -> Triple(
            Color(0xFFE8F5E8),
            Color(0xFF2E7D32),
            "Livrée"
        )
        DeliveryStatus.EN_ATTENTE -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFF57C00),
            "En attente"
        )
        DeliveryStatus.ANNULEE -> Triple(
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