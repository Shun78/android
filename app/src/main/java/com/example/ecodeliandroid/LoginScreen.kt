// app/src/main/java/com/example/ecodeliandroid/LoginScreen.kt
package com.example.ecodeliandroid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.ecodeliandroid.network.*
import com.example.ecodeliandroid.network.models.*
import com.example.ecodeliandroid.repository.AuthRepository
import com.google.gson.Gson

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // États du formulaire
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // États de l'interface
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Vérifier si déjà connecté
    LaunchedEffect(Unit) {
        if (authRepository.isLoggedIn()) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo et titre
        Icon(
            imageVector = Icons.Filled.LocalShipping,
            contentDescription = "EcoDeli",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EcoDeli",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Connectez-vous à votre compte",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Formulaire de connexion
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Champ email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = "Email")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMessage != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Champ mot de passe
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Mot de passe") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = "Mot de passe")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Masquer" else "Afficher"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMessage != null
                )

                // Message d'erreur
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bouton de connexion
                Button(
                    onClick = {
                        coroutineScope.launch {
                            loginUser(
                                email = email,
                                password = password,
                                context = context,
                                authRepository = authRepository,
                                navController = navController,
                                onLoading = { isLoading = it },
                                onError = { errorMessage = it }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isLoading) "Connexion..." else "Se connecter",
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lien mot de passe oublié
                TextButton(
                    onClick = { /* TODO: Implémenter mot de passe oublié */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mot de passe oublié ?")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lien d'inscription
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pas encore de compte ? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = { /* TODO: Navigation vers inscription */ }
            ) {
                Text("S'inscrire")
            }
        }
    }
}

// Fonction de connexion
private suspend fun loginUser(
    email: String,
    password: String,
    context: android.content.Context,
    authRepository: AuthRepository,
    navController: NavController,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)

    try {
        val apiService = GraphQLClient.createClient(context).create(GraphQLApiService::class.java)
        val gson = Gson()

        // Requête de connexion GraphQL
        val loginMutation = """
            mutation Login(${'}'}input: LoginInput!) {
            login(input: ${'}'}input) {
                token
            }
        }
        """

        val variables = mapOf(
            "input" to mapOf(
                "email" to email,
                "password" to password
            )
        )

        val request = GraphQLRequest(loginMutation, variables)
        val response = apiService.executeQuery(request)

        if (response.isSuccessful) {
            val body = response.body()
            if (body?.errors.isNullOrEmpty()) {
                // Extraire le token de la réponse
                val dataJson = gson.toJson(body?.data)
                val loginResponse = gson.fromJson(dataJson, object : com.google.gson.reflect.TypeToken<Map<String, Map<String, String>>>() {}.type) as Map<String, Map<String, String>>
                val token = loginResponse["login"]?.get("token")

                if (token != null) {
                    // Sauvegarder le token
                    authRepository.saveToken(token)

                    // Navigation vers l'écran principal
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    onError("Token de connexion invalide")
                }
            } else {
                val errorMsg = body?.errors?.firstOrNull()?.message ?: "Erreur de connexion"
                onError(errorMsg)
            }
        } else {
            onError("Erreur réseau: ${response.code()}")
        }
    } catch (e: Exception) {
        onError("Erreur de connexion: ${e.message}")
    } finally {
        onLoading(false)
    }
}