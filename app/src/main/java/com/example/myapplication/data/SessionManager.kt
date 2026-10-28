package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona la persistencia de la sesión de SegurApp usando [SharedPreferences].
 *
 * Solo se guarda un indicador de sesión activa y el nombre de usuario.
 * La contraseña NUNCA se almacena en el dispositivo.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Indica si existe una sesión iniciada previamente. */
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    /** Nombre del usuario con sesión activa (o null si no hay sesión). */
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    /** Guarda la sesión como activa. */
    fun saveSession(username: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    /** Elimina la sesión guardada (cerrar sesión). */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "segurapp_session"
        const val KEY_LOGGED_IN = "is_logged_in"
        const val KEY_USERNAME = "username"
    }
}
