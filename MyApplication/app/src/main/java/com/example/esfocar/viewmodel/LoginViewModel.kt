package com.example.esfocar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.esfocar.data.ProfesorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: ProfesorRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun haySesionActiva(): Boolean = repository.haySesionActiva()

    fun cambiarCorreo(valor: String) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(correo = valor, error = null, mensaje = null) }
    }

    fun cambiarContrasena(valor: String) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(contrasena = valor, error = null, mensaje = null) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.procesando) return

        val correoLimpio = estado.correo.trim().lowercase()
        val contrasena = estado.contrasena

        if (correoLimpio.isBlank() || contrasena.isBlank()) {
            _uiState.update { it.copy(error = "Por favor, completa ambos campos", mensaje = null) }
            return
        }

        _uiState.update { it.copy(procesando = true, error = null, mensaje = null) }

        viewModelScope.launch {
            try {
                val profesor = repository.iniciarSesion(correoLimpio, contrasena)
                if (profesor != null) {
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            loginExitoso = true,
                            mensaje = "¡Bienvenido/a, ${profesor.nombre}!"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            error = "Correo o contraseña incorrectos"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(procesando = false, error = "Error al iniciar sesión: ${e.message}")
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
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("ViewModel no reconocido: ${modelClass.name}")
        }
    }
}