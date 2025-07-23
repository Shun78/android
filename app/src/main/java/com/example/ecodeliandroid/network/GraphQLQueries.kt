// app/src/main/java/com/example/ecodeliandroid/network/GraphQLQueries.kt
package com.example.ecodeliandroid.network

object GraphQLQueries {

    // Query pour récupérer les informations utilisateur
    const val GET_ME = """
        query GetMe {
            me {
                id
                email
                firstName
                lastName
                phone
                avatar
                role
                createdAt
            }
        }
    """

    // Query pour récupérer la liste des tâches (livraisons)
    const val GET_TASKS = """
        query GetTasks(${'$'}filters: TaskFilters) {
            listTasks(filters: ${'$'}filters) {
                id
                title
                description
                type
                status
                calculatedPriceInCents
                fileUrl
                createdAt
                updatedAt
                address {
                    id
                    mainText
                    secondaryText
                    fullAddress
                }
                user {
                    id
                    firstName
                    lastName
                }
                category {
                    id
                    name
                    color
                }
                shipping {
                    packageCategory
                    pickupAddress {
                        mainText
                        secondaryText
                    }
                    deliveryAddress {
                        mainText
                        secondaryText
                    }
                }
            }
        }
    """

    // Query pour récupérer mes tâches créées
    const val GET_MY_TASKS = """
        query GetMyTasks {
            getMyTasks {
                id
                title
                description
                type
                status
                calculatedPriceInCents
                fileUrl
                createdAt
                updatedAt
                address {
                    mainText
                    secondaryText
                }
                category {
                    id
                    name
                    color
                }
                shipping {
                    packageCategory
                    pickupAddress {
                        mainText
                        secondaryText
                    }
                    deliveryAddress {
                        mainText
                        secondaryText
                    }
                }
                applications {
                    id
                    status
                    applicant {
                        firstName
                        lastName
                    }
                }
            }
        }
    """

    // Query pour récupérer mes candidatures
    const val GET_MY_APPLICATIONS = """
        query GetMyApplications {
            getMyApplications {
                id
                status
                message
                validationCode
                startedAt
                completedAt
                validatedAt
                createdAt
                task {
                    id
                    title
                    description
                    type
                    status
                    calculatedPriceInCents
                    address {
                        mainText
                        secondaryText
                    }
                    user {
                        firstName
                        lastName
                    }
                    category {
                        name
                        color
                    }
                    shipping {
                        packageCategory
                        pickupAddress {
                            mainText
                        }
                        deliveryAddress {
                            mainText
                        }
                    }
                }
            }
        }
    """

    // Query pour récupérer les détails d'une tâche
    const val GET_TASK_DETAILS = """
        query GetTask(${'$'}id: ID!) {
            getTask(id: ${'$'}id) {
                id
                title
                description
                type
                status
                calculatedPriceInCents
                fileUrl
                completedAt
                validatedAt
                createdAt
                updatedAt
                address {
                    id
                    mainText
                    secondaryText
                    fullAddress
                    lat
                    lng
                }
                user {
                    id
                    firstName
                    lastName
                    phone
                }
                category {
                    id
                    name
                    description
                    color
                }
                shipping {
                    id
                    packageCategory
                    packageDetails
                    estimatedDistanceInMeters
                    estimatedDurationInMinutes
                    calculatedPriceInCents
                    pickupAddress {
                        mainText
                        secondaryText
                        fullAddress
                    }
                    deliveryAddress {
                        mainText
                        secondaryText
                        fullAddress
                    }
                }
                applications {
                    id
                    status
                    message
                    validationCode
                    startedAt
                    completedAt
                    validatedAt
                    applicant {
                        id
                        firstName
                        lastName
                        phone
                    }
                }
                messages {
                    id
                    content
                    messageType
                    isRead
                    createdAt
                    sender {
                        id
                        firstName
                        lastName
                    }
                    receiver {
                        id
                        firstName
                        lastName
                    }
                }
            }
        }
    """

    // Mutation pour valider une livraison
    const val VALIDATE_TASK_COMPLETION = """
        mutation ValidateTaskCompletion(${'$'}taskId: ID!, ${'$'}validationCode: String!) {
            validateTaskCompletion(taskId: ${'$'}taskId, validationCode: ${'$'}validationCode) {
                id
                status
                validatedAt
            }
        }
    """

    // Mutation pour marquer les messages comme lus
    const val MARK_MESSAGES_AS_READ = """
        mutation MarkMessagesAsRead(${'$'}taskId: ID!) {
            markMessagesAsRead(taskId: ${'$'}taskId)
        }
    """
}