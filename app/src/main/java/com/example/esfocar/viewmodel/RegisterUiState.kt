package com.example.esfocar.viewmodel

data class RegisterUiState(
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val procesando: Boolean = false,
    val registroExitoso: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)