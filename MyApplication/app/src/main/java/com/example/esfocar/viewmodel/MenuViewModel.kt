package com.example.esfocar.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.esfocar.data.DocumentoRepository
import com.example.esfocar.data.ProfesorRepository
import com.example.esfocar.data.local.RubricaSerializador
import com.example.esfocar.data.local.dao.RubricaDao
import com.example.esfocar.data.local.entity.EvaluacionRubricaEntity
import com.example.esfocar.data.local.entity.PlantillaRubricaEntity
import com.example.esfocar.model.Documento
import com.example.esfocar.model.EvaluacionRubrica
import com.example.esfocar.model.ItemRubrica
import com.example.esfocar.model.PlantillaRubrica
import com.example.esfocar.util.RubricaPdfHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class MenuViewModel(
    private val profesorRepository: ProfesorRepository,
    private val documentoRepository: DocumentoRepository,
    private val rubricaDao: RubricaDao
) : ViewModel() {
    fun registrarEvaluacionEnPdfs(context: Context, evaluacion: EvaluacionRubrica) {
        val correo = _uiState.value.correoActivo
        if (correo.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val archivoOrigen = File(evaluacion.rutaPdf)
                if (!archivoOrigen.exists()) {
                    _uiState.update { it.copy(error = "El archivo PDF de la evaluación no existe") }
                    return@launch
                }

                val carpetaPdfs = File(context.filesDir, "pdfs").apply { if (!exists()) mkdirs() }
                val notaStr = String.format(Locale.US, "%.2f", evaluacion.notaFinal)
                val nombreLimpio = evaluacion.nombreEstudiante.trim().replace(" ", "_")
                val archivoDestino = File(carpetaPdfs, "${System.currentTimeMillis()}_Rubrica_${nombreLimpio}.pdf")

                archivoOrigen.copyTo(archivoDestino, overwrite = true)

                val nuevoDoc = Documento(
                    titulo = "Rúbrica · ${evaluacion.nombreEstudiante} (Nota $notaStr).pdf",
                    rutaOContenido = archivoDestino.absolutePath,
                    fechaCarga = System.currentTimeMillis(),
                    correoProfesor = correo
                )
                documentoRepository.agregar(nuevoDoc)
                _uiState.update {
                    it.copy(mensaje = "PDF descargado y añadido a la pestaña PDFs")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error al registrar PDF: ${e.message}")
                }
            }
        }
    }
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        val correo = profesorRepository.obtenerCorreoActivo().orEmpty()
        val nombre = profesorRepository.obtenerNombreActivo().orEmpty()

        _uiState.update {
            it.copy(
                correoActivo = correo,
                nombreActivo = nombre,
                cargando = false
            )
        }

        if (correo.isNotBlank()) {
            viewModelScope.launch {
                documentoRepository.observarPorProfesor(correo).collect { listaDocs ->
                    _uiState.update { it.copy(documentacionDocs = listaDocs) }
                }
            }

            viewModelScope.launch {
                rubricaDao.obtenerPlantillas(correo).collect { lista ->
                    _uiState.update { it.copy(plantillas = lista.map { e -> e.toModel() }) }
                }
            }

            viewModelScope.launch {
                rubricaDao.obtenerEvaluaciones(correo).collect { lista ->
                    _uiState.update { it.copy(evaluaciones = lista.map { e -> e.toModel() }) }
                }
            }
        }
    }

    fun agregarDocumento(titulo: String, rutaLocal: String) {
        val correo = _uiState.value.correoActivo
        if (correo.isBlank()) {
            _uiState.update { it.copy(error = "No hay sesión activa") }
            return
        }

        viewModelScope.launch {
            try {
                val nuevoDoc = Documento(
                    titulo = titulo.trim().ifBlank { "Documento.pdf" },
                    rutaOContenido = rutaLocal,
                    fechaCarga = System.currentTimeMillis(),
                    correoProfesor = correo
                )
                documentoRepository.agregar(nuevoDoc)
                _uiState.update { it.copy(mensaje = "Documento cargado correctamente") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar documento: ${e.message}") }
            }
        }
    }

    fun abrirDocumento(documento: Documento) {
        _uiState.update { it.copy(documentoSeleccionado = documento) }
    }

    fun cerrarDocumento() {
        _uiState.update { it.copy(documentoSeleccionado = null) }
    }

    fun eliminarDocumento(documento: Documento) {
        viewModelScope.launch {
            try {
                if (documento.rutaOContenido.startsWith("/")) {
                    runCatching { File(documento.rutaOContenido).delete() }
                }
                documentoRepository.eliminar(documento)
                _uiState.update { it.copy(mensaje = "Documento eliminado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar: ${e.message}") }
            }
        }
    }


    fun abrirNuevaPlantilla() {
        _uiState.update { it.copy(plantillaEnEdicion = null, mostrarEditorPlantilla = true) }
    }

    fun abrirEditarPlantilla(plantilla: PlantillaRubrica) {
        _uiState.update { it.copy(plantillaEnEdicion = plantilla, mostrarEditorPlantilla = true) }
    }

    fun cerrarEditorPlantilla() {
        _uiState.update { it.copy(plantillaEnEdicion = null, mostrarEditorPlantilla = false) }
    }

    fun guardarPlantilla(
        plantillaExistente: PlantillaRubrica?,
        titulo: String,
        items: List<ItemRubrica>
    ) {
        if (titulo.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa un título para la rúbrica") }
            return
        }
        if (items.isEmpty() || items.any { it.criterio.isBlank() }) {
            _uiState.update { it.copy(error = "Completa la descripción de todos los ítems") }
            return
        }

        val correo = _uiState.value.correoActivo
        if (correo.isBlank()) return

        // Se guarda sin niveles seleccionados (plantilla limpia)
        val itemsLimpios = items.map { it.copy(nivelSeleccionado = null) }
        val puntajeMaximo = itemsLimpios.sumOf { it.puntajeMaximo() }
        val itemsJson = RubricaSerializador.toJson(itemsLimpios)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val modelo = PlantillaRubrica(
                    id = plantillaExistente?.id ?: 0,
                    profesorCorreo = correo,
                    titulo = titulo.trim(),
                    itemsJson = itemsJson,
                    puntajeMaximo = puntajeMaximo,
                    fechaCreacion = plantillaExistente?.fechaCreacion ?: System.currentTimeMillis()
                )
                val entity = PlantillaRubricaEntity.fromModel(modelo)
                if (plantillaExistente == null) {
                    rubricaDao.insertarPlantilla(entity)
                } else {
                    rubricaDao.actualizarPlantilla(entity)
                }
                _uiState.update {
                    it.copy(
                        mostrarEditorPlantilla = false,
                        plantillaEnEdicion = null,
                        mensaje = "Rúbrica guardada. Ahora puedes usarla para evaluar alumnos."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar rúbrica: ${e.message}") }
            }
        }
    }

    fun eliminarPlantilla(plantilla: PlantillaRubrica) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                rubricaDao.eliminarPlantilla(PlantillaRubricaEntity.fromModel(plantilla))
                _uiState.update { it.copy(mensaje = "Rúbrica eliminada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar: ${e.message}") }
            }
        }
    }


    fun iniciarEvaluacion(plantilla: PlantillaRubrica) {
        _uiState.update {
            it.copy(
                plantillaParaEvaluar = plantilla,
                evaluacionEnEdicion = null
            )
        }
    }

    fun abrirEditarEvaluacion(evaluacion: EvaluacionRubrica) {
        _uiState.update {
            it.copy(
                plantillaParaEvaluar = null,
                evaluacionEnEdicion = evaluacion
            )
        }
    }

    fun cerrarDialogoEvaluacion() {
        _uiState.update {
            it.copy(
                plantillaParaEvaluar = null,
                evaluacionEnEdicion = null
            )
        }
    }

    fun guardarEvaluacion(
        context: Context,
        plantillaId: Int,
        tituloRubrica: String,
        nombreEstudiante: String,
        itemsEvaluados: List<ItemRubrica>,
        evaluacionExistente: EvaluacionRubrica? = null
    ) {
        if (nombreEstudiante.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa el nombre completo del estudiante") }
            return
        }
        if (itemsEvaluados.any { it.nivelSeleccionado == null }) {
            _uiState.update { it.copy(error = "Selecciona un nivel de logro en todos los ítems") }
            return
        }

        val correo = _uiState.value.correoActivo
        if (correo.isBlank()) return

        val puntajeObtenido = itemsEvaluados.sumOf { it.puntajeObtenido() }
        val puntajeMaximo = itemsEvaluados.sumOf { it.puntajeMaximo() }
        val notaFinal = RubricaPdfHelper.calcularNotaChilena(puntajeObtenido, puntajeMaximo)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val docenteNombre = _uiState.value.nombreActivo.ifBlank { correo }
                val rutaPdf = RubricaPdfHelper.generarPdfRubrica(
                    context = context,
                    tituloRubrica = tituloRubrica,
                    nombreEstudiante = nombreEstudiante.trim(),
                    docente = docenteNombre,
                    items = itemsEvaluados,
                    puntajeObtenido = puntajeObtenido,
                    puntajeMaximo = puntajeMaximo,
                    notaFinal = notaFinal,
                    rutaAnterior = evaluacionExistente?.rutaPdf
                )

                val ahora = System.currentTimeMillis()
                val modelo = EvaluacionRubrica(
                    id = evaluacionExistente?.id ?: 0,
                    plantillaId = plantillaId,
                    profesorCorreo = correo,
                    tituloRubrica = tituloRubrica,
                    nombreEstudiante = nombreEstudiante.trim(),
                    itemsJson = RubricaSerializador.toJson(itemsEvaluados),
                    puntajeObtenido = puntajeObtenido,
                    puntajeMaximo = puntajeMaximo,
                    notaFinal = notaFinal,
                    rutaPdf = rutaPdf,
                    fechaEvaluacion = ahora
                )

                val entity = EvaluacionRubricaEntity.fromModel(modelo)
                if (evaluacionExistente == null) {
                    rubricaDao.insertarEvaluacion(entity)
                } else {
                    rubricaDao.actualizarEvaluacion(entity)
                }

                val notaFormateada = String.format(Locale.US, "%.2f", notaFinal)
                val docPreview = Documento(
                    id = modelo.id.toLong(),
                    titulo = "Evaluación · ${modelo.nombreEstudiante} (Nota $notaFormateada)",
                    rutaOContenido = rutaPdf,
                    fechaCarga = ahora,
                    correoProfesor = correo
                )

                _uiState.update {
                    it.copy(
                        plantillaParaEvaluar = null,
                        evaluacionEnEdicion = null,
                        documentoSeleccionado = docPreview,
                        mensaje = "Evaluación guardada · Nota: $notaFormateada"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar evaluación: ${e.message}") }
            }
        }
    }

    fun verPdfEvaluacion(evaluacion: EvaluacionRubrica) {
        val notaFormateada = String.format(Locale.US, "%.2f", evaluacion.notaFinal)
        val doc = Documento(
            id = evaluacion.id.toLong(),
            titulo = "${evaluacion.tituloRubrica} · ${evaluacion.nombreEstudiante} ($notaFormateada)",
            rutaOContenido = evaluacion.rutaPdf,
            fechaCarga = evaluacion.fechaEvaluacion,
            correoProfesor = evaluacion.profesorCorreo
        )
        _uiState.update { it.copy(documentoSeleccionado = doc) }
    }

    fun eliminarEvaluacion(evaluacion: EvaluacionRubrica) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (evaluacion.rutaPdf.isNotBlank()) {
                    runCatching { File(evaluacion.rutaPdf).delete() }
                }
                rubricaDao.eliminarEvaluacion(EvaluacionRubricaEntity.fromModel(evaluacion))
                _uiState.update { it.copy(mensaje = "Evaluación eliminada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar evaluación: ${e.message}") }
            }
        }
    }

    // ==================== SESIÓN Y MENSAJES ====================

    fun cerrarSesion() {
        profesorRepository.cerrarSesion()
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }

    class Factory(
        private val profesorRepository: ProfesorRepository,
        private val documentoRepository: DocumentoRepository,
        private val rubricaDao: RubricaDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
                return MenuViewModel(profesorRepository, documentoRepository, rubricaDao) as T
            }
            throw IllegalArgumentException("ViewModel no reconocido: ${modelClass.name}")
        }
    }
}
