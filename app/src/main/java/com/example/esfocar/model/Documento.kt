package com.example.esfocar.model

data class Documento(
    val id: Long = 0L,
    val titulo: String,
    val rutaOContenido: String,
    val fechaCarga: Long,
    val correoProfesor: String
)