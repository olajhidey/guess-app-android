package com.taloscore.guessapp.data.remote

import com.taloscore.guessapp.data.model.CategoryResponse
import com.taloscore.guessapp.data.model.LoginForm
import com.taloscore.guessapp.data.model.LoginResponse
import com.taloscore.guessapp.data.model.RegisterForm
import com.taloscore.guessapp.data.model.RegisterResponse
import com.taloscore.guessapp.data.model.TopicResponse
import com.taloscore.guessapp.viewmodel.Result
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/api/auth/login")
    suspend fun loginRequest(@Body request: LoginForm): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun registerRequest(@Body request: RegisterForm): Response<RegisterResponse>

    @GET("/api/category/list")
    suspend fun getCategoryList(@Header("Authorization") token: String): Response<List<CategoryResponse>>

    @GET("/api/topic/list/{id}")
    suspend fun getTopicList(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<List<TopicResponse>>

    @POST("/api/game/create")
    suspend fun saveGameSession(
        @Body request: Result,
        @Header("Authorization") token: String
    ): Response<Any>

    @GET("/api/game/list/{gameCode}")
    suspend fun listParticipants(
        @Header("Authorization") token: String,
        @Path("gameCode") gameCode: String
    ): Response<List<Result>>
}