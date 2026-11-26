package com.example.esfocar.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esfocar.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    val bgDark = Color(0xFF0E1310)
    val surfaceDark = Color(0xFF161D19)
    val verdeAccent = Color(0xFF4CAF7D)
    val doradoDetalle = Color(0xFFC5A028)
    val textoSecundario = Color(0xFF94A39B)

    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.error, uiState.mensaje) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
        uiState.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            HorizontalDivider(
                modifier = Modifier.width(36.dp),
                thickness = 3.dp,
                color = doradoDetalle
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ESFOCAR",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color.White
            )

            Text(
                text = "Acceso Institucional",
                fontSize = 14.sp,
                color = textoSecundario,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo Correo
            OutlinedTextField(
                value = uiState.correo,
                onValueChange = { viewModel.cambiarCorreo(it) },
                label = { Text("Correo institucional") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = textoSecundario
                    )
                },
                singleLine = true,
                enabled = !uiState.procesando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = verdeAccent,
                    unfocusedBorderColor = Color(0xFF28352E),
                    focusedLabelColor = verdeAccent,
                    unfocusedLabelColor = textoSecundario,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = verdeAccent,
                    focusedContainerColor = surfaceDark,
                    unfocusedContainerColor = surfaceDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = uiState.contrasena,
                onValueChange = { viewModel.cambiarContrasena(it) },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = textoSecundario
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Mostrar contraseña",
                            tint = textoSecundario
                        )
                    }
                },
                singleLine = true,
                enabled = !uiState.procesando,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = verdeAccent,
                    unfocusedBorderColor = Color(0xFF28352E),
                    focusedLabelColor = verdeAccent,
                    unfocusedLabelColor = textoSecundario,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = verdeAccent,
                    focusedContainerColor = surfaceDark,
                    unfocusedContainerColor = surfaceDark
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón principal
            Button(
                onClick = { viewModel.iniciarSesion() },
                enabled = !uiState.procesando,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0B4F32),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, verdeAccent.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.procesando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Ingresar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Enlace a Registro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    color = textoSecundario,
                    fontSize = 13.sp
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    enabled = !uiState.procesando
                ) {
                    Text(
                        text = "Registrarse",
                        color = verdeAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}