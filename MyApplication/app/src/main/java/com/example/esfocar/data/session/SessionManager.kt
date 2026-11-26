package com.example.esfocar.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("esfocar_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CORREO_ACTIVO = "correo_usuario_activo"
        private const val KEY_NOMBRE_ACTIVO = "nombre_usuario_activo"
    }

    fun guardarSesion(correo: String, nombre: String) {
        prefs.edit()
            .putString(KEY_CORREO_ACTIVO, correo)
            .putString(KEY_NOMBRE_ACTIVO, nombre)
            .apply()
    }

    fun haySesionActiva(): Boolean {
        return !prefs.getString(KEY_CORREO_ACTIVO, null).isNullOrBlank()
    }

    fun obtenerCorreoActivo(): String? {
        return prefs.getString(KEY_CORREO_ACTIVO, null)
    }

    fun obtenerNombreActivo(): String? {
        return prefs.getString(KEY_NOMBRE_ACTIVO, "")
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}