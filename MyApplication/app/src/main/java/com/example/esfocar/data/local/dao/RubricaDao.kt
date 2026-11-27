package com.example.esfocar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.esfocar.data.local.entity.EvaluacionRubricaEntity
import com.example.esfocar.data.local.entity.PlantillaRubricaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RubricaDao {
    @Query("SELECT * FROM plantillas_rubrica WHERE profesorCorreo = :correo ORDER BY fechaCreacion DESC")
    fun obtenerPlantillas(correo: String): Flow<List<PlantillaRubricaEntity>>

    @Query("SELECT * FROM plantillas_rubrica WHERE profesorCorreo = :correo AND sincronizado = 0")
    suspend fun obtenerPlantillasPendientes(correo: String): List<PlantillaRubricaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPlantilla(plantilla: PlantillaRubricaEntity): Long

    @Update
    suspend fun actualizarPlantilla(plantilla: PlantillaRubricaEntity)

    @Delete
    suspend fun eliminarPlantilla(plantilla: PlantillaRubricaEntity)

    @Query("DELETE FROM plantillas_rubrica WHERE id = :id")
    suspend fun eliminarPlantillaPorId(id: Int)

    @Query("SELECT * FROM evaluaciones_rubrica WHERE profesorCorreo = :correo ORDER BY fechaEvaluacion DESC")
    fun obtenerEvaluaciones(correo: String): Flow<List<EvaluacionRubricaEntity>>

    @Query("SELECT * FROM evaluaciones_rubrica WHERE profesorCorreo = :correo AND sincronizado = 0")
    suspend fun obtenerEvaluacionesPendientes(correo: String): List<EvaluacionRubricaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvaluacion(evaluacion: EvaluacionRubricaEntity): Long

    @Update
    suspend fun actualizarEvaluacion(evaluacion: EvaluacionRubricaEntity)

    @Delete
    suspend fun eliminarEvaluacion(evaluacion: EvaluacionRubricaEntity)

    @Query("DELETE FROM evaluaciones_rubrica WHERE id = :id")
    suspend fun eliminarEvaluacionPorId(id: Int)
}