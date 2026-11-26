package com.example.esfocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.esfocar.data.ProfesorRepository
import com.example.esfocar.model.Profesor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: ProfesorRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun cambiarNombre(valor: String) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(nombre = valor, error = null, mensaje = null) }
    }

    fun cambiarApellidos(valor: String) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(apellidos = valor, error = null, mensaje = null) }
    }

    fun cambiarCorreo(valor: String) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(correo = valor, error = null, mensaje = null) }
    }

    fun cambiarContrasena(valor: String) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(contrasena = valor, error = null, mensaje = null) }
    }

    fun registrar() {
        val estado = _uiState.value
        if (estado.procesando) return

        val correoLimpio = estado.correo.trim().lowercase()
        val nombreLimpio = estado.nombre.trim()
        val apellidosLimpios = estado.apellidos.trim()
        val contrasena = estado.contrasena

        if (
            nombreLimpio.isBlank() ||
            apellidosLimpios.isBlank() ||
            correoLimpio.isBlank() ||
            contrasena.isBlank()
        ) {
            _uiState.update {
                it.copy(error = "Por favor, completa todos los campos", mensaje = null)
            }
            return
        }

        _uiState.update { it.copy(procesando = true, error = null, mensaje = null) }

        viewModelScope.launch {
            try {
                val nuevoProfesor = Profesor(
                    correo = correoLimpio,
                    password = contrasena,
                    nombre = nombreLimpio,
                    apellidos = apellidosLimpios
                )
                val registrado = repository.registrar(nuevoProfesor)
                if (registrado) {
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            registroExitoso = true,
                            mensaje = "¡Registro exitoso!"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            error = "El correo ya está registrado"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(procesando = false, error = "Error al registrar: ${e.message}")
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }

    class Factory(private val repository: ProfesorRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(repository) as T
            }
            throw IllegalArgumentException("ViewModel no reconocido: ${modelClass.name}")
        }
    }
}