package com.example.esfocar.data

import android.content.Context
import com.example.esfocar.data.local.AppDatabase
import com.example.esfocar.data.local.entity.EvaluacionRubricaEntity
import com.example.esfocar.data.local.entity.PlantillaRubricaEntity
import com.example.esfocar.data.remote.RetrofitClient
import com.example.esfocar.data.remote.RubricasApiService
import com.example.esfocar.model.EvaluacionRubrica
import com.example.esfocar.model.PlantillaRubrica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RubricaRepository(
    context: Context,
    private val api: RubricasApiService = RetrofitClient.rubricasApi
) {

    private val rubricaDao = AppDatabase.obtenerInstancia(context).rubricaDao()

    fun observarPlantillas(correo: String): Flow<List<PlantillaRubrica>> {
        return rubricaDao.obtenerPlantillas(correo).map { lista ->
            lista.map { it.toModel() }
        }
    }

    fun observarEvaluaciones(correo: String): Flow<List<EvaluacionRubrica>> {
        return rubricaDao.obtenerEvaluaciones(correo).map { lista ->
            lista.map { it.toModel() }
        }
    }

    suspend fun guardarPlantilla(plantilla: PlantillaRubrica, esEdicion: Boolean): Boolean = withContext(Dispatchers.IO) {
        // 1. Guarda primero en Room local como pendiente
        val idLocal = if (esEdicion) {
            rubricaDao.actualizarPlantilla(PlantillaRubricaEntity.fromModel(plantilla, sincronizado = false))
            plantilla.id
        } else {
            rubricaDao.insertarPlantilla(PlantillaRubricaEntity.fromModel(plantilla, sincronizado = false)).toInt()
        }

        // 2. Intenta sincronizar con el microservicio Spring Boot
        val sincronizado = runCatching {
            val response = if (esEdicion) {
                api.actualizarPlantilla(idLocal, plantilla.copy(id = idLocal))
            } else {
                api.crearPlantilla(plantilla.copy(id = 0))
            }
            if (response.isSuccessful && response.body() != null) {
                val remota = response.body()!!
                if (!esEdicion && remota.id != idLocal) {
                    rubricaDao.eliminarPlantillaPorId(idLocal)
                }
                rubricaDao.insertarPlantilla(PlantillaRubricaEntity.fromModel(remota, sincronizado = true))
                true
            } else {
                false
            }
        }.getOrDefault(false)

        sincronizado
    }

    suspend fun eliminarPlantilla(plantilla: PlantillaRubrica) = withContext(Dispatchers.IO) {
        rubricaDao.eliminarPlantillaPorId(plantilla.id)
        runCatching { api.eliminarPlantilla(plantilla.id) }
    }

    suspend fun guardarEvaluacion(evaluacion: EvaluacionRubrica, esEdicion: Boolean): Boolean = withContext(Dispatchers.IO) {
        // 1. Guarda primero en Room local
        val idLocal = if (esEdicion) {
            rubricaDao.actualizarEvaluacion(EvaluacionRubricaEntity.fromModel(evaluacion, sincronizado = false))
            evaluacion.id
        } else {
            rubricaDao.insertarEvaluacion(EvaluacionRubricaEntity.fromModel(evaluacion, sincronizado = false)).toInt()
        }

        // 2. Intenta enviar al microservicio Spring Boot
        val sincronizado = runCatching {
            val response = if (esEdicion) {
                api.actualizarEvaluacion(idLocal, evaluacion.copy(id = idLocal))
            } else {
                api.crearEvaluacion(evaluacion.copy(id = 0))
            }
            if (response.isSuccessful && response.body() != null) {
                val remota = response.body()!!.copy(rutaPdf = evaluacion.rutaPdf)
                if (!esEdicion && remota.id != idLocal) {
                    rubricaDao.eliminarEvaluacionPorId(idLocal)
                }
                rubricaDao.insertarEvaluacion(EvaluacionRubricaEntity.fromModel(remota, sincronizado = true))
                true
            } else {
                false
            }
        }.getOrDefault(false)

        sincronizado
    }

    suspend fun eliminarEvaluacion(evaluacion: EvaluacionRubrica) = withContext(Dispatchers.IO) {
        rubricaDao.eliminarEvaluacionPorId(evaluacion.id)
        runCatching { api.eliminarEvaluacion(evaluacion.id) }
    }

    /**
     * Sube los registros creados sin conexión y descarga los del servidor.
     */
    suspend fun sincronizarConServidor(correo: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Subir plantillas creadas offline
            val plantillasPendientes = rubricaDao.obtenerPlantillasPendientes(correo)
            for (pendiente in plantillasPendientes) {
                val resp = api.crearPlantilla(pendiente.toModel().copy(id = 0))
                if (resp.isSuccessful && resp.body() != null) {
                    rubricaDao.eliminarPlantillaPorId(pendiente.id)
                    rubricaDao.insertarPlantilla(PlantillaRubricaEntity.fromModel(resp.body()!!, sincronizado = true))
                }
            }

            // 2. Subir evaluaciones creadas offline
            val evaluacionesPendientes = rubricaDao.obtenerEvaluacionesPendientes(correo)
            for (pendiente in evaluacionesPendientes) {
                val resp = api.crearEvaluacion(pendiente.toModel().copy(id = 0))
                if (resp.isSuccessful && resp.body() != null) {
                    val remotaConRuta = resp.body()!!.copy(rutaPdf = pendiente.rutaPdf)
                    rubricaDao.eliminarEvaluacionPorId(pendiente.id)
                    rubricaDao.insertarEvaluacion(EvaluacionRubricaEntity.fromModel(remotaConRuta, sincronizado = true))
                }
            }

            // 3. Descargar datos actualizados desde Neon
            val remotasPlantillas = api.obtenerPlantillas(correo)
            remotasPlantillas.forEach { remota ->
                rubricaDao.insertarPlantilla(PlantillaRubricaEntity.fromModel(remota, sincronizado = true))
            }

            val remotasEvaluaciones = api.obtenerEvaluaciones(correo)
            remotasEvaluaciones.forEach { remota ->
                rubricaDao.insertarEvaluacion(EvaluacionRubricaEntity.fromModel(remota, sincronizado = true))
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}