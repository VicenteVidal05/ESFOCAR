package com.example.esfocar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.esfocar.data.local.entity.ProfesorEntity
@Dao
interface ProfesorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(profesorEntity: ProfesorEntity): Long

    @Query("SELECT * FROM profesor WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): ProfesorEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM profesor WHERE correo = :correo)")
    suspend fun existeCorreo(correo: String): Boolean

    @Query("UPDATE profesor SET nombre = :nuevoNombre WHERE correo = :correo")
    suspend fun actualizarNombre(correo: String, nuevoNombre: String): Int

    @Delete
    suspend fun eliminar(profesor: ProfesorEntity)
}