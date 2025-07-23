// app/src/main/java/com/example/ecodeliandroid/ProfileScreen.kt
package com.example.ecodeliandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.ecodeliandroid.repository.EcoDeliRepository
import com.example.ecodeliandroid.repository.AuthRepository
import com.example.ecodeliandroid.network.models.*

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { EcoDeliRepository(context) }
    val authRepository = remember { AuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // États pour l'interface
    var user by remember { mutableStateOf<User?>(null) }
    var myTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var myApplications by remember { mutableStateOf<List<TaskApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // États pour les dialogs
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Charger les données utilisateur
    LaunchedEffect(Unit) {
        isLoading = true
        error = null

        try {
            // Charger les informations utilisateur
            val userResult = repository.getMe()
            userResult.fold(
                onSuccess = { userData ->
                    user = userData
                },
                onFailure = { exception ->
                    error = "Erreur lors du chargement du profil: ${exception.message}"
                }
            )

            // Charger les statistiques (mes tâches et candidatures)
            val tasksResult = repository.getMyTasks()
            tasksResult.fold(
                onSuccess = { tasks ->
                    myTasks = tasks
                },
                onFailure = { /* Erreur non bloquante */ }
            )

            val applicationsResult = repository.getMyApplications()
            applicationsResult.fold(
                onSuccess = { applications ->
                    myApplications = applications
                },
                onFailure = { /* Erreur non bloquante */ }
            )

        } catch (e: Exception) {
            error = "Erreur réseau: ${e.message}"
        } finally {
            isLoading = false
        }
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
            ErrorScreen(
                error = error!!,
                onRetry = {
                    isLoading = true
                    error = null
                }
            )
        }
        user != null -> {
            ProfileContent(
                user = user!!,
                myTasks = myTasks,
                myApplications = myApplications,
                onEditProfile = { showEditDialog = true },
                onLogout = { showLogoutDialog = true }
            )
        }
    }

    // Dialog d'édition du profil
    if (showEditDialog && user != null) {
        EditProfileDialog(
            user = user!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedUser ->
                // TODO: Implémenter la mutation updateUser
                showEditDialog = false
                // user = updatedUser
            }
        )
    }

    // Dialog de confirmation de déconnexion
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Déconnexion") },
            text = { Text("Êtes-vous sûr de vouloir vous déconnecter ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            authRepository.clearToken()
                            showLogoutDialog = false
                            // Navigation vers l'écran de connexion
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Déconnexion", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun ProfileContent(
    user: User,
    myTasks: List<Task>,
    myApplications: List<TaskApplication>,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // En-tête du profil
        ProfileHeaderCard(user = user)

        Spacer(modifier = Modifier.height(24.dp))

        // Informations de contact
        ContactInfoCard(user = user)

        Spacer(modifier = Modifier.height(24.dp))

        // Actions du profil
        ProfileActionsCard(onEditProfile = onEditProfile)

        Spacer(modifier = Modifier.height(24.dp))

        // Statistiques utilisateur
        StatisticsCard(
            myTasks = myTasks,
            myApplications = myApplications
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton de déconnexion
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Se déconnecter", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileHeaderCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            if (user.avatar != null) {
                AsyncImage(
                    model = user.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = buildString {
                        user.firstName?.firstOrNull()?.let { append(it) }
                        user.lastName?.firstOrNull()?.let { append(it) }
                    }.ifEmpty { user.email.first().toString() }

                    Text(
                        text = initials,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${user.firstName.orEmpty()} ${user.lastName.orEmpty()}".trim()
                    .ifEmpty { "Utilisateur" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = user.email,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge de rôle
            RoleChip(role = user.role)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Membre depuis ${formatDate(user.createdAt)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ContactInfoCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Informations de contact",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (user.phone != null) {
                ProfileInfoRow(
                    icon = Icons.Filled.Phone,
                    label = "Téléphone",
                    value = user.phone
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            ProfileInfoRow(
                icon = Icons.Filled.Email,
                label = "Email",
                value = user.email
            )
        }
    }
}

@Composable
fun ProfileActionsCard(onEditProfile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
            )

            ProfileActionItem(
                icon = Icons.Filled.Edit,
                title = "Modifier le profil",
                subtitle = "Mettre à jour vos informations",
                onClick = onEditProfile
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            ProfileActionItem(
                icon = Icons.Filled.Lock,
                title = "Changer le mot de passe",
                subtitle = "Sécuriser votre compte",
                onClick = { /* Action pour changer le mot de passe */ }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            ProfileActionItem(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                subtitle = "Gérer vos préférences",
                onClick = { /* Action pour gérer les notifications */ }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            ProfileActionItem(
                icon = Icons.Filled.Help,
                title = "Aide & Support",
                subtitle = "Besoin d'assistance ?",
                onClick = { /* Action pour l'aide */ }
            )
        }
    }
}

@Composable
fun StatisticsCard(
    myTasks: List<Task>,
    myApplications: List<TaskApplication>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Mes statistiques",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = myTasks.size.toString(),
                    label = "Tâches créées"
                )

                StatisticItem(
                    value = myApplications.size.toString(),
                    label = "Candidatures"
                )

                val completedTasks = myApplications.count {
                    it.status == ApplicationStatus.VALIDATED
                }
                StatisticItem(
                    value = completedTasks.toString(),
                    label = "Complétées"
                )
            }
        }
    }
}

@Composable
fun RoleChip(role: Role) {
    val (backgroundColor, textColor, text) = when (role) {
        Role.BASIC -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2),
            "Utilisateur"
        )
        Role.PARTNER -> Triple(
            Color(0xFFE8F5E8),
            Color(0xFF2E7D32),
            "Partenaire"
        )
        Role.ADMIN -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFF57C00),
            "Administrateur"
        )
        Role.SUPER_ADMIN -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            "Super Admin"
        )
        Role.TESTER -> Triple(
            Color(0xFFF3E5F5),
            Color(0xFF7B1FA2),
            "Testeur"
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
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
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

@Composable
fun ProfileActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Aller",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StatisticItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var firstName by remember { mutableStateOf(user.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(user.lastName.orEmpty()) }
    var phone by remember { mutableStateOf(user.phone.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le profil") },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Prénom") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedUser = user.copy(
                        firstName = firstName.ifBlank { null },
                        lastName = lastName.ifBlank { null },
                        phone = phone.ifBlank { null }
                    )
                    onSave(updatedUser)
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Réessayer")
                }
            }
        }
    }
}