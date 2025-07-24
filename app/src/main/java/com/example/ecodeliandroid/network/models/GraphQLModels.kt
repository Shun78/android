// app/src/main/java/com/example/ecodeliandroid/network/models/GraphQLModels.kt
package com.example.ecodeliandroid.network.models

import com.google.gson.annotations.SerializedName

// Classes pour les requêtes/réponses GraphQL
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any>? = null
)

data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<GraphQLError>?
)

data class GraphQLError(
    val message: String,
    val locations: List<Location>?,
    val path: List<String>?
)

data class Location(
    val line: Int,
    val column: Int
)

// Modèles de données GraphQL
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val type: TaskType,
    val status: TaskStatus,
    val address: Address,
    val user: User,
    val category: Category?,
    val shipping: Shipping?,
    val applications: List<TaskApplication>?,
    val messages: List<TaskMessage>?,
    val calculatedPriceInCents: Int?,
    val fileUrl: String?,
    val completedAt: String?,
    val validatedAt: String?,
    val estimatedDuration: Int?,
    val createdAt: String,
    val updatedAt: String
)

data class TaskApplication(
    val id: String,
    val taskId: String,
    val applicantId: String,
    val applicant: User,
    val task: Task,
    val status: ApplicationStatus,
    val message: String,
    val validationCode: String?,
    val startedAt: String?,
    val completedAt: String?,
    val validatedAt: String?,
    val createdAt: String,
    val updatedAt: String
)

data class TaskMessage(
    val id: String,
    val taskId: String,
    val senderId: String,
    val sender: User,
    val receiverId: String,
    val receiver: User,
    val content: String,
    val messageType: MessageType,
    val isRead: Boolean,
    val createdAt: String
)

data class User(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val avatar: String?,
    val role: Role,
    val createdAt: String
)

data class Address(
    val id: String,
    val mainText: String,
    val secondaryText: String,
    val lat: Double,
    val lng: Double,
    val placeId: String,
    val fullAddress: String,
    val locationType: String
)

data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val amountInCents: Int,
    val fileUrl: String?
)

data class Shipping(
    val id: String,
    val taskId: String,
    val packageCategory: PackageCategory,
    val pickupAddress: Address,
    val deliveryAddress: Address,
    val packageDetails: Map<String, Any>?,
    val estimatedDistanceInMeters: Double?,
    val estimatedDurationInMinutes: Double?,
    val calculatedPriceInCents: Int?
)

// Enums corrigés selon le backend
enum class TaskType { SERVICE, SHIPPING }
enum class TaskStatus { DRAFT, PUBLISHED, IN_PROGRESS, COMPLETED, DONE, CANCELLED }

// ApplicationStatus corrigé selon le backend (SANS IN_PROGRESS)
enum class ApplicationStatus { PENDING, ACCEPTED, REJECTED, COMPLETED, VALIDATED }

enum class MessageType { TEXT, VALIDATION_CODE, SYSTEM }
enum class PackageCategory { SMALL, MEDIUM, LARGE }
enum class Role { BASIC, ADMIN, PARTNER, SUPER_ADMIN, TESTER }

// Wrapper pour les réponses
data class TasksResponse(val listTasks: List<Task>)
data class MyTasksResponse(val getMyTasks: List<Task>)
data class MyApplicationsResponse(val getMyApplications: List<TaskApplication>)
data class TaskDetailsResponse(val getTask: Task)
data class TaskMessagesResponse(val getTaskMessages: List<TaskMessage>)
data class MeResponse(val me: User)