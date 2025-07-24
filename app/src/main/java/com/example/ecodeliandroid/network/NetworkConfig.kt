package com.example.ecodeliandroid.network

object NetworkConfig {
    // Configuration de l'URL du backend
    const val BASE_URL = "http://10.0.2.2:4000/" // Pour émulateur Android
    //const val BASE_URL = "http://192.168.1.100:4000/" // Remplacez par votre IP
    // const val BASE_URL = "http://192.168.1.XXX:4000/" // Pour device physique
    const val GRAPHQL_ENDPOINT = "${BASE_URL}graphql"

    // Headers par défaut
    const val CONTENT_TYPE = "application/json"
    const val ACCEPT = "application/json"
}