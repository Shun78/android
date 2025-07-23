package com.example.ecodeliandroid.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.ecodeliandroid.network.models.*

interface GraphQLApiService {
    @POST("graphql")
    suspend fun executeQuery(@Body request: GraphQLRequest): Response<GraphQLResponse<Any>>
}