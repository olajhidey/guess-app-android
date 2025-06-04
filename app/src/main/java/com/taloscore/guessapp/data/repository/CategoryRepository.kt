package com.taloscore.guessapp.data.repository

import android.content.Context
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.data.model.CategoryResponse
import com.taloscore.guessapp.data.model.LoginResponse
import com.taloscore.guessapp.data.model.TopicResponse
import com.taloscore.guessapp.data.remote.RetrofitInstance
import com.taloscore.guessapp.utils.Common
import com.taloscore.guessapp.utils.TokenManager
import com.taloscore.guessapp.viewmodel.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class CategoryRepository @Inject constructor(@ApplicationContext val context: Context) {
    private val apiService = RetrofitInstance.apiService
    fun list(token: String): Flow<ApiState<List<CategoryResponse>>> =
        makeApiCall { apiService.getCategoryList(token) }

    fun listTopics(id:String, token: String): Flow<ApiState<List<TopicResponse>>> =
        makeApiCall { apiService.getTopicList(id, token) }

    fun createGame(token: String, result: Result): Flow<ApiState<Any>> =makeApiCall {
        apiService.saveGameSession(result, token)
    }

    fun listParticipants(token: String, gameCode: String): Flow<ApiState<List<Result>>> =
        makeApiCall { apiService.listParticipants(token, gameCode) }


    private inline fun <T> makeApiCall(
        crossinline apiCall: suspend () -> Response<T>
    ): Flow<ApiState<T>> = flow {
        emit(ApiState.Loading)

        try {
            val response = apiCall()
            val status = response.code()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(ApiState.Success(it))
                } ?: emit(ApiState.Error("Response body is null"))
            } else {
                val errorMessage = response.errorBody()?.string()?.let {
                    Common.parseApiError(it)?.error
                } ?: "Unknown error"

                emit(ApiState.Error(errorMessage))
            }
        } catch (err: Exception) {
            err.printStackTrace()
            emit(ApiState.Error(err.message ?: "An unexpected error occurred"))
        }
    }
}