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

    /**
     * Obtiene la lista completa de documentos ordenados por fecha descendente
     * Al ser un `Flow`, la UI reacciona automaticamente
     * a cualquier cambio que ocurra en la tabla
     */
    @Query("SELECT * FROM documento ORDER BY fecha_carga DESC")
    fun obtenerTodos(): Flow<List<DocumentoEntity>>

    /**
     * Obtiene los documentos de un profesor concreto, filtrados por su correo
     * Al ser un `Flow`, la UI reacciona automaticamente a cambios en la tabla
     */
    @Query("SELECT * FROM documento WHERE correo_profesor = :correo ORDER BY fecha_carga DESC")
    fun obtenerPorProfesor(correo: String): Flow<List<DocumentoEntity>>

     // Inserta un documento, Si el id coincide con uno existente, lo reemplaza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(documento: DocumentoEntity): Long

     // Elimina un documento recibido como parametro
    @Delete
    suspend fun eliminar(documento: DocumentoEntity)

     // Elimina un documento usando su identificador numerico (ID)
    @Query("DELETE FROM documento WHERE id = :documentoId")
    suspend fun eliminarPorId(documentoId: Long)
}