package com.taloscore.guessapp.data.repository

import android.content.Context
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.data.model.LoginForm
import com.taloscore.guessapp.data.model.LoginResponse
import com.taloscore.guessapp.data.model.RegisterForm
import com.taloscore.guessapp.data.model.RegisterResponse
import com.taloscore.guessapp.data.remote.RetrofitInstance
import com.taloscore.guessapp.utils.Common
import com.taloscore.guessapp.utils.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(@ApplicationContext val context: Context) {
    private val apiService = RetrofitInstance.apiService
    fun login(loginForm: LoginForm): Flow<ApiState<LoginResponse>> =
        makeApiCall { apiService.loginRequest(loginForm) }

    fun register(registerForm: RegisterForm): Flow<ApiState<RegisterResponse>> =
        makeApiCall { apiService.registerRequest(registerForm) }

    private inline fun <T> makeApiCall(
        crossinline apiCall: suspend () -> Response<T>
    ): Flow<ApiState<T>> = flow {
        emit(ApiState.Loading)

        try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    if (it is LoginResponse){
                        TokenManager.writeToken(context = context, it.token)
                    }
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