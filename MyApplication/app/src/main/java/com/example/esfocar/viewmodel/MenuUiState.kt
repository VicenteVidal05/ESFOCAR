package com.example.esfocar.viewmodel

import com.example.esfocar.data.remote.dto.FeriadoExternoDto
import com.example.esfocar.model.Documento
import com.example.esfocar.model.EvaluacionRubrica
import com.example.esfocar.model.PlantillaRubrica

data class MenuUiState(
    val nombreActivo: String = "",
    val correoActivo: String = "",
    val documentacionDocs: List<Documento> = emptyList(),
    val plantillas: List<PlantillaRubrica> = emptyList(),
    val evaluaciones: List<EvaluacionRubrica> = emptyList(),
    val feriadosExternos: List<FeriadoExternoDto> = emptyList(),
    val cargandoApiExterna: Boolean = false,
    val documentoSeleccionado: Documento? = null,
    val plantillaEnEdicion: PlantillaRubrica? = null,
    val mostrarEditorPlantilla: Boolean = false,
    val plantillaParaEvaluar: PlantillaRubrica? = null,
    val evaluacionEnEdicion: EvaluacionRubrica? = null,
    val cargando: Boolean = true,
    val mensaje: String? = null,
    val error: String? = null
)