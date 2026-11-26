package com.example.esfocar.viewmodel

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val procesando: Boolean = false,
    val loginExitoso: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)