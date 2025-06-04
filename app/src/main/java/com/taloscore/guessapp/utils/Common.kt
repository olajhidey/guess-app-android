package com.taloscore.guessapp.utils

import com.google.gson.Gson
import com.taloscore.guessapp.data.model.ErrorResponse

object Common {
    fun parseApiError(error: String?): ErrorResponse? {
        val errorResponse = error?.let { Gson().fromJson(it, ErrorResponse::class.java) }
        return errorResponse
    }

}