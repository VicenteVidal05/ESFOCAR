package com.example.esfocar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.esfocar.data.local.entity.ProfesorEntity

@Dao
interface ProfesorDao {

    /**
     * Inserta un nuevo profesor, si ya existe un correo duplicado,
     * ignora la insercion en lugar de fallar
     */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(profesorEntity: ProfesorEntity): Long

    /**
     * Busca un profesor por su correo
     * Se usa para el login, si el correo existe y la contraseña coincide, el usuario inicia sesion
     * Devuelve null cuando no hay ningun usuario con ese correo
     */
    @Query("SELECT * FROM profesor WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): ProfesorEntity?

     // Verifica si un correo ya esta registrado, evita registrar dos veces la misma cuenta
    @Query("SELECT EXISTS(SELECT 1 FROM profesor WHERE correo = :correo)")
    suspend fun existeCorreo(correo: String): Boolean

     // Actualiza el nombre de un usuario identificado por su correo.
    @Query("UPDATE profesor SET nombre = :nuevoNombre WHERE correo = :correo")
    suspend fun actualizarNombre(correo: String, nuevoNombre: String): Int

     // Elimina un profesor de la base de datos
    @Delete
    suspend fun eliminar(profesor: ProfesorEntity)
}