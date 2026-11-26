package com.example.esfocar.data

import android.content.Context
import com.example.esfocar.data.local.AppDatabase
import com.example.esfocar.data.local.entity.DocumentoEntity
import com.example.esfocar.model.Documento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DocumentoRepository(context: Context) {

    private val documentoDao = AppDatabase.obtenerInstancia(context).documentoDao()

    fun observarPorProfesor(correo: String): Flow<List<Documento>> {
        return documentoDao.obtenerPorProfesor(correo).map { lista ->
            lista.map { entity ->
                Documento(
                    id = entity.id,
                    titulo = entity.titulo,
                    rutaOContenido = entity.rutaOContenido,
                    fechaCarga = entity.fechaCarga,
                    correoProfesor = entity.correoProfesor
                )
            }
        }
    }

    suspend fun agregar(documento: Documento): Boolean = withContext(Dispatchers.IO) {
        val entity = DocumentoEntity(
            id = documento.id,
            titulo = documento.titulo,
            rutaOContenido = documento.rutaOContenido,
            fechaCarga = documento.fechaCarga,
            correoProfesor = documento.correoProfesor
        )
        documentoDao.insertar(entity) != -1L
    }

    suspend fun eliminar(documento: Documento) = withContext(Dispatchers.IO) {
        val entity = DocumentoEntity(
            id = documento.id,
            titulo = documento.titulo,
            rutaOContenido = documento.rutaOContenido,
            fechaCarga = documento.fechaCarga,
            correoProfesor = documento.correoProfesor
        )
        documentoDao.eliminar(entity)
    }
}