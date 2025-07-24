// app/src/main/java/com/example/ecodeliandroid/repository/EcoDeliRepository.kt
package com.example.ecodeliandroid.repository

import android.content.Context
import android.util.Log
import com.example.ecodeliandroid.network.*
import com.example.ecodeliandroid.network.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EcoDeliRepository(private val context: Context) {

    private val apiService: GraphQLApiService by lazy {
        GraphQLClient.createClient(context).create(GraphQLApiService::class.java)
    }

    private val gson = Gson()

    // Logs de debug pour vérifier la configuration réseau
    init {
        Log.d("EcoDeliRepository", "=== EcoDeliRepository Initialization ===")
        Log.d("EcoDeliRepository", "Tentative de connexion à: ${NetworkConfig.BASE_URL}")
        Log.d("EcoDeliRepository", "URL complète GraphQL: ${NetworkConfig.BASE_URL}graphql")
        Log.d("EcoDeliRepository", "========================================")
    }

    /**
     * Récupère les informations de l'utilisateur connecté
     */
    suspend fun getMe(): Result<User> = withContext(Dispatchers.IO) {
        try {
            Log.d("EcoDeliRepository", "Getting user info...")
            val request = GraphQLRequest(GraphQLQueries.GET_ME)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    Log.d("EcoDeliRepository", "User data: $dataJson")
                    val meResponse = gson.fromJson(dataJson, MeResponse::class.java)
                    Result.success(meResponse.me)
                } else {
                    val errorMsg = body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"
                    Log.e("EcoDeliRepository", "GraphQL error: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Erreur réseau: ${response.code()} - ${response.message()}"
                Log.e("EcoDeliRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("EcoDeliRepository", "Exception in getMe: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Récupère la liste des tâches (livraisons et prestations)
     */
    suspend fun getTasks(filters: Map<String, Any>? = null): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            Log.d("EcoDeliRepository", "Getting tasks with filters: $filters")
            val variables = filters?.let { mapOf("filters" to it) }
            val request = GraphQLRequest(GraphQLQueries.GET_TASKS, variables)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    Log.d("EcoDeliRepository", "Tasks data: $dataJson")
                    val tasksResponse = gson.fromJson(dataJson, TasksResponse::class.java)
                    Result.success(tasksResponse.listTasks)
                } else {
                    val errorMsg = body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"
                    Log.e("EcoDeliRepository", "GraphQL error in getTasks: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Erreur réseau: ${response.code()} - ${response.message()}"
                Log.e("EcoDeliRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("EcoDeliRepository", "Exception in getTasks: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Récupère mes tâches créées
     */
    suspend fun getMyTasks(): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            Log.d("EcoDeliRepository", "Getting my tasks...")
            val request = GraphQLRequest(GraphQLQueries.GET_MY_TASKS)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    Log.d("EcoDeliRepository", "My tasks data: $dataJson")
                    val myTasksResponse = gson.fromJson(dataJson, MyTasksResponse::class.java)
                    Result.success(myTasksResponse.getMyTasks)
                } else {
                    val errorMsg = body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"
                    Log.e("EcoDeliRepository", "GraphQL error in getMyTasks: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Erreur réseau: ${response.code()} - ${response.message()}"
                Log.e("EcoDeliRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("EcoDeliRepository", "Exception in getMyTasks: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Récupère mes candidatures avec gestion d'erreur améliorée
     */
    suspend fun getMyApplications(): Result<List<TaskApplication>> = withContext(Dispatchers.IO) {
        try {
            Log.d("EcoDeliRepository", "Getting my applications...")
            val request = GraphQLRequest(GraphQLQueries.GET_MY_APPLICATIONS)
            val response = apiService.executeQuery(request)

            Log.d("EcoDeliRepository", "Response code: ${response.code()}")
            Log.d("EcoDeliRepository", "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("EcoDeliRepository", "Response body: $body")

                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    Log.d("EcoDeliRepository", "Applications data: $dataJson")

                    try {
                        val applicationsResponse = gson.fromJson(dataJson, MyApplicationsResponse::class.java)
                        Log.d("EcoDeliRepository", "Successfully parsed ${applicationsResponse.getMyApplications.size} applications")
                        Result.success(applicationsResponse.getMyApplications)
                    } catch (e: Exception) {
                        Log.e("EcoDeliRepository", "Error parsing applications: ${e.message}", e)
                        // Retourner une liste vide en cas d'erreur de parsing
                        Result.success(emptyList())
                    }
                } else {
                    val errorMsg = body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"
                    Log.e("EcoDeliRepository", "GraphQL errors: ${body?.errors}")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur réseau: ${response.code()} - ${response.message()}"
                Log.e("EcoDeliRepository", "$errorMsg. Error body: $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("EcoDeliRepository", "Exception in getMyApplications: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Récupère les détails d'une tâche
     */
    suspend fun getTaskDetails(taskId: String): Result<Task> = withContext(Dispatchers.IO) {
        try {
            Log.d("EcoDeliRepository", "Getting task details for ID: $taskId")
            val variables = mapOf("id" to taskId)
            val request = GraphQLRequest(GraphQLQueries.GET_TASK_DETAILS, variables)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    Log.d("EcoDeliRepository", "Task details data: $dataJson")
                    val taskResponse = gson.fromJson(dataJson, TaskDetailsResponse::class.java)
                    Result.success(taskResponse.getTask)
                } else {
                    val errorMsg = body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"
                    Log.e("EcoDeliRepository", "GraphQL error in getTaskDetails: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Erreur réseau: ${response.code()} - ${response.message()}"
                Log.e("EcoDeliRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("EcoDeliRepository", "Exception in getTaskDetails: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Valide la completion d'une tâche avec un code
     */
    suspend fun validateTaskCompletion(taskId: String, validationCode: String): Result<Task> = withContext(Dispatchers.IO) {
        try {
            Log.d("EcoDeliRepository", "Validating task completion for ID: $taskId")
            val variables = mapOf(
                "taskId" to taskId,
                "validationCode" to validationCode
            )
            val request = GraphQLRequest(GraphQLQueries.VALIDATE_TASK_COMPLETION, variables)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    Log.d("EcoDeliRepository", "Validation data: $dataJson")
                    val taskResponse = gson.fromJson(dataJson, object : TypeToken<Map<String, Task>>() {}.type) as Map<String, Task>
                    Result.success(taskResponse["validateTaskCompletion"]!!)
                } else {
                    val errorMsg = body?.errors?.firstOrNull()?.message ?: "Code de validation incorrect"
                    Log.e("EcoDeliRepository", "GraphQL error in validateTaskCompletion: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Erreur réseau: ${response.code()} - ${response.message()}"
                Log.e("EcoDeliRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("EcoDeliRepository", "Exception in validateTaskCompletion: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Marque les messages comme lus
     */
    suspend fun markMessagesAsRead(taskId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("EcoDeliRepository", "Marking messages as read for task ID: $taskId")
            val variables = mapOf("taskId" to taskId)
            val request = GraphQLRequest(GraphQLQueries.MARK_MESSAGES_AS_READ, variables)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    Log.d("EcoDeliRepository", "Successfully marked messages as read")
                    Result.success(true)
                } else {
                    val errorMsg = body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"
                    Log.e("EcoDeliRepository", "GraphQL error in markMessagesAsRead: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "Erreur réseau: ${response.code()} - ${response.message()}"
                Log.e("EcoDeliRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("EcoDeliRepository", "Exception in markMessagesAsRead: ${e.message}", e)
            Result.failure(e)
        }
    }
}
