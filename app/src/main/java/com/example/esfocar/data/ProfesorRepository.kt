package com.example.esfocar.data

import android.content.Context
import com.example.esfocar.data.local.AppDatabase
import com.example.esfocar.data.local.entity.ProfesorEntity
import com.example.esfocar.data.session.SessionManager
import com.example.esfocar.model.Profesor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfesorRepository(context: Context) {

    private val profesorDao = AppDatabase.obtenerInstancia(context).profesorDao()
    private val sessionManager = SessionManager(context)

    fun haySesionActiva(): Boolean = sessionManager.haySesionActiva()

    fun obtenerCorreoActivo(): String = sessionManager.obtenerCorreoActivo() ?: ""

    fun obtenerNombreActivo(): String = sessionManager.obtenerNombreActivo() ?: ""

    fun cerrarSesion() {
        sessionManager.cerrarSesion()
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): Profesor? = withContext(Dispatchers.IO) {
        val entity = profesorDao.buscarPorCorreo(correo)
        if (entity != null && entity.password == contrasena) {
            val profesor = Profesor(
                correo = entity.correo,
                password = entity.password,
                nombre = entity.nombre,
                apellidos = entity.apellidos
            )
            sessionManager.guardarSesion(
                correo = profesor.correo,
                nombre = "${profesor.nombre} ${profesor.apellidos}"
            )
            profesor
        } else {
            null
        }
    }

    suspend fun registrar(profesor: Profesor): Boolean = withContext(Dispatchers.IO) {
        if (profesorDao.existeCorreo(profesor.correo)) {
            return@withContext false
        }
        val entity = ProfesorEntity(
            correo = profesor.correo,
            password = profesor.password,
            nombre = profesor.nombre,
            apellidos = profesor.apellidos
        )
        val resultado = profesorDao.insertar(entity)
        if (resultado != -1L) {
            sessionManager.guardarSesion(
                correo = profesor.correo,
                nombre = "${profesor.nombre} ${profesor.apellidos}"
            )
            true
        } else {
            false
        }
    }
}