package com.example.esfocar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documento")
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "ruta_o_contenido")
    val rutaOContenido: String,

    @ColumnInfo(name = "fecha_carga")
    val fechaCarga: Long,

    @ColumnInfo(name = "correo_profesor")
    val correoProfesor: String
)