package com.example.ecowayer.service

import android.content.Context
import com.example.ecowayer.model.Usuario

/** Sessão persistida apenas no dispositivo. O token continua sendo HttpOnly no backend. */
object SessionStore {
    private const val PREFS = "ecowayer_session"
    private const val USER_ID = "user_id"
    private const val USER_NAME = "user_name"
    private const val USER_EMAIL = "user_email"

    private lateinit var preferences: android.content.SharedPreferences

    fun initialize(context: Context) {
        if (!::preferences.isInitialized) {
            preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    fun saveUser(usuario: Usuario) {
        preferences.edit()
            .putInt(USER_ID, usuario.id)
            .putString(USER_NAME, usuario.nome)
            .putString(USER_EMAIL, usuario.email)
            .apply()
    }

    fun currentUser(): Usuario? {
        val id = preferences.getInt(USER_ID, 0)
        return if (id > 0) Usuario(id, preferences.getString(USER_NAME, "") ?: "", preferences.getString(USER_EMAIL, "") ?: "") else null
    }

    fun isLoggedIn() = currentUser() != null

    fun clear() {
        preferences.edit().clear().apply()
    }
}
