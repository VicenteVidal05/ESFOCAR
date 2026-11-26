package com.example.esfocar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.esfocar.data.local.entity.DocumentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {

    @Query("SELECT * FROM documento ORDER BY fecha_carga DESC")
    fun obtenerTodos(): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documento WHERE correo_profesor = :correo ORDER BY fecha_carga DESC")
    fun obtenerPorProfesor(correo: String): Flow<List<DocumentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(documento: DocumentoEntity): Long

    @Delete
    suspend fun eliminar(documento: DocumentoEntity)

    @Query("DELETE FROM documento WHERE id = :documentoId")
    suspend fun eliminarPorId(documentoId: Long)
}