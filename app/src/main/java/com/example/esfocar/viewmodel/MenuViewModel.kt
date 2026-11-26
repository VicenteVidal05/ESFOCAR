package com.example.esfocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.esfocar.data.DocumentoRepository
import com.example.esfocar.data.ProfesorRepository
import com.example.esfocar.model.Documento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MenuViewModel(
    private val documentoRepository: DocumentoRepository,
    private val profesorRepository: ProfesorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MenuUiState(
            nombreActivo = profesorRepository.obtenerNombreActivo(),
            correoActivo = profesorRepository.obtenerCorreoActivo()
        )
    )
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        observarDocumentos()
    }

    private fun observarDocumentos() {
        val correo = _uiState.value.correoActivo
        if (correo.isBlank()) {
            _uiState.update { it.copy(cargando = false) }
            return
        }

        viewModelScope.launch {
            documentoRepository.observarPorProfesor(correo).collect { lista ->
                _uiState.update {
                    it.copy(
                        documentos = lista,
                        cargando = false
                    )
                }
            }
        }
    }

    fun agregarDocumento(titulo: String, uriString: String) {
        val correo = _uiState.value.correoActivo
        if (correo.isBlank()) return

        viewModelScope.launch {
            try {
                val nuevoDoc = Documento(
                    titulo = titulo,
                    rutaOContenido = uriString,
                    fechaCarga = System.currentTimeMillis(),
                    correoProfesor = correo
                )
                documentoRepository.agregar(nuevoDoc)
                _uiState.update {
                    it.copy(mensaje = "PDF agregado correctamente", error = null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "No se pudo agregar el PDF: ${e.message}", mensaje = null)
                }
            }
        }
    }

    fun eliminarDocumento(documento: Documento) {
        viewModelScope.launch {
            try {
                documentoRepository.eliminar(documento)
                _uiState.update {
                    it.copy(mensaje = "Documento eliminado", error = null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "No se pudo eliminar el documento: ${e.message}", mensaje = null)
                }
            }
        }
    }

    fun cerrarSesion() {
        profesorRepository.cerrarSesion()
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }

    class Factory(
        private val documentoRepository: DocumentoRepository,
        private val profesorRepository: ProfesorRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
                return MenuViewModel(documentoRepository, profesorRepository) as T
            }
            throw IllegalArgumentException("ViewModel no reconocido: ${modelClass.name}")
        }
    }
}