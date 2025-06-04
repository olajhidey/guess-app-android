package com.taloscore.guessapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.taloscore.guessapp.utils.Constant.TOKEN_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object TokenManager {
    val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "app_pref_settings")

    suspend fun writeToken(context: Context, token: String) {
        val TOKEN_PREF = stringPreferencesKey(TOKEN_KEY)
        context.datastore.edit { settings ->
            val currentToken = settings[TOKEN_PREF] ?: ""
            settings[TOKEN_PREF] = token
        }
    }

    suspend fun writeToPreference(context: Context, key: String, value: String){
        val keyReference = stringPreferencesKey(key)
        context.datastore.edit { settings ->
            val currentValue = settings[keyReference] ?: ""
            settings[keyReference] = value
        }
    }

    fun readValueInStore(context: Context, key: String): Flow<String>{
        val keyReference = stringPreferencesKey(key)
        val preferences = context.datastore.data.map { settings ->
            settings[keyReference] ?: ""
        }
        return preferences
    }

     fun readToken(context: Context): Flow<String> {
        val TOKEN_PREF = stringPreferencesKey(TOKEN_KEY)
        val preferences = context.datastore.data.map { settings->
             settings[TOKEN_PREF] ?: ""
        }
        return preferences
    }
}