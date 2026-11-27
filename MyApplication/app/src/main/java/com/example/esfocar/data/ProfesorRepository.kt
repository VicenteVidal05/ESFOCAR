package com.example.esfocar.data

import android.content.Context
import com.example.esfocar.data.local.AppDatabase
import com.example.esfocar.data.local.entity.ProfesorEntity
import com.example.esfocar.data.remote.RetrofitClient
import com.example.esfocar.data.remote.UsuariosApiService
import com.example.esfocar.data.remote.dto.LoginRequestDto
import com.example.esfocar.data.session.SessionManager
import com.example.esfocar.model.Profesor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfesorRepository(
    context: Context,
    private val api: UsuariosApiService = RetrofitClient.usuariosApi
) {

    private val profesorDao = AppDatabase.obtenerInstancia(context).profesorDao()
    private val sessionManager = SessionManager(context)

    fun haySesionActiva(): Boolean = sessionManager.haySesionActiva()

    fun obtenerCorreoActivo(): String = sessionManager.obtenerCorreoActivo() ?: ""

    fun obtenerNombreActivo(): String = sessionManager.obtenerNombreActivo() ?: ""

    fun cerrarSesion() {
        sessionManager.cerrarSesion()
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): Profesor? = withContext(Dispatchers.IO) {
        // 1. Intenta validar contra el microservicio remoto
        val profesorRemoto = runCatching {
            val response = api.login(LoginRequestDto(correo = correo, password = contrasena))
            if (response.isSuccessful) response.body() else null
        }.getOrNull()

        if (profesorRemoto != null) {
            // Guarda/actualiza copia local en Room
            profesorDao.insertar(
                ProfesorEntity(
                    correo = profesorRemoto.correo,
                    password = profesorRemoto.password,
                    nombre = profesorRemoto.nombre,
                    apellidos = profesorRemoto.apellidos
                )
            )
            sessionManager.guardarSesion(
                correo = profesorRemoto.correo,
                nombre = "${profesorRemoto.nombre} ${profesorRemoto.apellidos}"
            )
            return@withContext profesorRemoto
        }

        // 2. Respaldo offline: busca en Room local
        val local = profesorDao.buscarPorCorreo(correo)
        if (local != null && local.password == contrasena) {
            val profesorLocal = Profesor(
                correo = local.correo,
                password = local.password,
                nombre = local.nombre,
                apellidos = local.apellidos
            )
            // Intenta sincronizar usuario local hacia el backend si no existía allá
            runCatching { api.registrar(profesorLocal) }

            sessionManager.guardarSesion(
                correo = profesorLocal.correo,
                nombre = "${profesorLocal.nombre} ${profesorLocal.apellidos}"
            )
            profesorLocal
        } else {
            null
        }
    }

    suspend fun registrar(profesor: Profesor): Boolean = withContext(Dispatchers.IO) {
        if (profesorDao.existeCorreo(profesor.correo)) {
            return@withContext false
        }

        // Intenta registrar en el microservicio
        val resultadoRemoto = runCatching { api.registrar(profesor) }.getOrNull()
        if (resultadoRemoto != null && resultadoRemoto.code() == 409) {
            return@withContext false // Ya existe en servidor
        }

        // Guarda siempre en Room local
        val entity = ProfesorEntity(
            correo = profesor.correo,
            password = profesor.password,
            nombre = profesor.nombre,
            apellidos = profesor.apellidos
        )
        val insertado = profesorDao.insertar(entity) != -1L
        if (insertado) {
            sessionManager.guardarSesion(
                correo = profesor.correo,
                nombre = "${profesor.nombre} ${profesor.apellidos}"
            )
        }
        insertado
    }
}