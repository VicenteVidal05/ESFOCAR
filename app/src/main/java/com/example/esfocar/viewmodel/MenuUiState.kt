package com.example.esfocar.viewmodel

import com.example.esfocar.model.Documento

data class MenuUiState(
    val nombreActivo: String = "",
    val correoActivo: String = "",
    val documentos: List<Documento> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String? = null,
    val error: String? = null
)