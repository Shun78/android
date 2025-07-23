// app/src/main/java/com/example/ecodeliandroid/repository/EcoDeliRepository.kt
package com.example.ecodeliandroid.repository

import android.content.Context
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

    /**
     * Récupère les informations de l'utilisateur connecté
     */
    suspend fun getMe(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(GraphQLQueries.GET_ME)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    val meResponse = gson.fromJson(dataJson, MeResponse::class.java)
                    Result.success(meResponse.me)
                } else {
                    Result.failure(Exception(body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"))
                }
            } else {
                Result.failure(Exception("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère la liste des tâches (livraisons et prestations)
     */
    suspend fun getTasks(filters: Map<String, Any>? = null): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val variables = filters?.let { mapOf("filters" to it) }
            val request = GraphQLRequest(GraphQLQueries.GET_TASKS, variables)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    val tasksResponse = gson.fromJson(dataJson, TasksResponse::class.java)
                    Result.success(tasksResponse.listTasks)
                } else {
                    Result.failure(Exception(body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"))
                }
            } else {
                Result.failure(Exception("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère mes tâches créées
     */
    suspend fun getMyTasks(): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(GraphQLQueries.GET_MY_TASKS)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    val myTasksResponse = gson.fromJson(dataJson, MyTasksResponse::class.java)
                    Result.success(myTasksResponse.getMyTasks)
                } else {
                    Result.failure(Exception(body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"))
                }
            } else {
                Result.failure(Exception("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère mes candidatures
     */
    suspend fun getMyApplications(): Result<List<TaskApplication>> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequest(GraphQLQueries.GET_MY_APPLICATIONS)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    val applicationsResponse = gson.fromJson(dataJson, MyApplicationsResponse::class.java)
                    Result.success(applicationsResponse.getMyApplications)
                } else {
                    Result.failure(Exception(body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"))
                }
            } else {
                Result.failure(Exception("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère les détails d'une tâche
     */
    suspend fun getTaskDetails(taskId: String): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val variables = mapOf("id" to taskId)
            val request = GraphQLRequest(GraphQLQueries.GET_TASK_DETAILS, variables)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    val dataJson = gson.toJson(body?.data)
                    val taskResponse = gson.fromJson(dataJson, TaskDetailsResponse::class.java)
                    Result.success(taskResponse.getTask)
                } else {
                    Result.failure(Exception(body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"))
                }
            } else {
                Result.failure(Exception("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Valide la completion d'une tâche avec un code
     */
    suspend fun validateTaskCompletion(taskId: String, validationCode: String): Result<Task> = withContext(Dispatchers.IO) {
        try {
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
                    val taskResponse = gson.fromJson(dataJson, object : TypeToken<Map<String, Task>>() {}.type) as Map<String, Task>
                    Result.success(taskResponse["validateTaskCompletion"]!!)
                } else {
                    Result.failure(Exception(body?.errors?.firstOrNull()?.message ?: "Code de validation incorrect"))
                }
            } else {
                Result.failure(Exception("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marque les messages comme lus
     */
    suspend fun markMessagesAsRead(taskId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val variables = mapOf("taskId" to taskId)
            val request = GraphQLRequest(GraphQLQueries.MARK_MESSAGES_AS_READ, variables)
            val response = apiService.executeQuery(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.errors.isNullOrEmpty()) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body?.errors?.firstOrNull()?.message ?: "Erreur inconnue"))
                }
            } else {
                Result.failure(Exception("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}