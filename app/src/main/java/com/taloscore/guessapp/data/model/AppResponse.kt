package com.taloscore.guessapp.data.model

data class LoginResponse(val token: String)
data class RegisterResponse(val message: String)
data class ErrorResponse(val error: String)