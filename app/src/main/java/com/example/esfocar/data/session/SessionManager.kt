package com.example.esfocar.data.session

import android.content.Context
import android.content.SharedPreferences

/**
 * Manejador simple de la sesión del usuario guardando los datos esenciales
 * (como el correo del usuario autenticado) usando SharedPreferences.
 * Esto permite persistir el estado de inicio de sesión entre reinicios de la app.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("esfocar_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CORREO_ACTIVO = "correo_usuario_activo"
        private const val KEY_NOMBRE_ACTIVO = "nombre_usuario_activo"
    }

     // Guarda la sesión activa tras un login o registro exitoso.
    fun guardarSesion(correo: String, nombre: String) {
        prefs.edit()
            .putString(KEY_CORREO_ACTIVO, correo)
            .putString(KEY_NOMBRE_ACTIVO, nombre)
            .apply()
    }

     // Retorna true si hay un usuario logueado en SharedPreferences.
    fun haySesionActiva(): Boolean {
        return !prefs.getString(KEY_CORREO_ACTIVO, null).isNullOrBlank()
    }

     // Retorna el correo del usuario autenticado actualmente (o null si no hay ninguno).
    fun obtenerCorreoActivo(): String? {
        return prefs.getString(KEY_CORREO_ACTIVO, null)
    }

     // Retorna el nombre del usuario autenticado actualmente.
    fun obtenerNombreActivo(): String? {
        return prefs.getString(KEY_NOMBRE_ACTIVO, "")
    }

     // Cierra la sesión activa eliminando la información almacenada.
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}