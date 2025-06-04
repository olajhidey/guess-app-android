package com.taloscore.guessapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.data.model.LoginForm
import com.taloscore.guessapp.data.model.LoginResponse
import com.taloscore.guessapp.data.model.RegisterForm
import com.taloscore.guessapp.data.model.RegisterResponse
import com.taloscore.guessapp.data.repository.AuthRepository
import com.taloscore.guessapp.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(@ApplicationContext context: Context, private val authRepository: AuthRepository): ViewModel() {
    private val _loginState = MutableStateFlow<ApiState<LoginResponse>?>(null)
    val loginState = _loginState

    private val _registerState = MutableStateFlow<ApiState<RegisterResponse>?>(null)
    val registerState = _registerState

    private val _token = MutableStateFlow<String?>("")
    val token = _token

    val isLoading = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            TokenManager.readToken(context).firstOrNull().let {
                _token.value = it
            }
            isLoading.value = false
        }
    }

    fun performLogin(loginForm: LoginForm){
       viewModelScope.launch {
           _loginState.value = ApiState.Loading
           authRepository.login(loginForm).collect {
               _loginState.value = it
           }
       }
    }

    fun performRegistration(registerForm: RegisterForm) {
        viewModelScope.launch {
            _registerState.value = ApiState.Loading
            authRepository.register(registerForm).collect {
                _registerState.value = it
            }
        }
    }
}