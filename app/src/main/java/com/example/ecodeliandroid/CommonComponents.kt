// app/src/main/java/com/example/ecodeliandroid/CommonComponents.kt
package com.example.ecodeliandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecodeliandroid.network.models.*

// Composant pour les messages d'état vide
@Composable
fun EmptyStateMessage(message: String, icon: ImageVector = Icons.Filled.Info) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Chip pour le statut des tâches
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

// Chip pour le statut des candidatures
@Composable
fun ApplicationStatusChip(status: ApplicationStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        ApplicationStatus.PENDING -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFF57C00),
            "En attente"
        )
        ApplicationStatus.ACCEPTED -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "Acceptée"
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

// Chip pour la catégorie des colis
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

// Classe utilitaire pour les quadruples
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// Fonctions utilitaires de formatage
fun formatDate(dateString: String): String {
    return try {
        val instant = java.time.Instant.parse(dateString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString.substring(0, 10)
    }
}

fun formatPrice(priceInCents: Int?): String {
    return if (priceInCents != null) {
        val euros = priceInCents / 100.0
        String.format("%.2f€", euros)
    } else {
        "Prix à définir"
    }
}

fun formatDuration(durationInMinutes: Int): String {
    val hours = durationInMinutes / 60
    val minutes = durationInMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
}

fun formatDuration(durationInMinutes: Double): String {
    val hours = (durationInMinutes / 60).toInt()
    val minutes = (durationInMinutes % 60).toInt()

    return when {
        hours > 0 -> "${hours}h ${minutes}min"
        else -> "${minutes}min"
    }
}