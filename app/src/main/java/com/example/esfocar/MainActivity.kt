package com.example.esfocar

import android.app.ActivityOptions
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import com.example.esfocar.ui.theme.ESFOCARTheme
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.esfocar.data.local.AppDatabase
import com.example.esfocar.data.session.SessionManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)
        if (sessionManager.haySesionActiva()) {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
            return
        }

        setContent {
            ESFOCARTheme {
                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        var correo by remember { mutableStateOf("") }
                        var contrasena by remember { mutableStateOf("") }
                        Text("ESFOCAR Iniciar sesión")
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = { Text("Correo") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = { contrasena = it },
                            label = { Text("Contraseña") },
                            visualTransformation =
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text="No tienes una cuenta? Registrate", color = Color.Cyan,
                            modifier = Modifier.clickable{
                                // Animación de transición al registrarse
                                val opciones = ActivityOptions.makeCustomAnimation(
                                    context,
                                    R.anim.entrada_bounce,
                                    android.R.anim.fade_out
                                )
                                context.startActivity(
                                    Intent(context, RegisterActivity::class.java),
                                    opciones.toBundle()
                                )
                            })
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            val correoLimpio = correo.trim().lowercase()
                            if (correoLimpio.isNotBlank() && contrasena.isNotBlank()) {
                                coroutineScope.launch {
                                    val db = AppDatabase.obtenerInstancia(context)
                                    val profesor = db.profesorDao().buscarPorCorreo(correoLimpio)

                                    if (profesor != null && profesor.password == contrasena) {
                                        SessionManager(context).guardarSesion(
                                            correo = profesor.correo,
                                            nombre = "${profesor.nombre} ${profesor.apellidos}"
                                        )
                                        Toast.makeText(context, "¡Bienvenido/a, ${profesor.nombre}!", Toast.LENGTH_SHORT).show()
                                        // Animación de transición al iniciar sesión
                                        val opciones = ActivityOptions.makeCustomAnimation(
                                            context,
                                            R.anim.entrada_bounce,
                                            android.R.anim.fade_out
                                        )
                                        val intent = Intent(context, MenuActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent, opciones.toBundle())
                                    } else {
                                        Toast.makeText(context, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show()
                            }
                        }) {Text("Iniciar sesion") }
                    }
                }
            }
        }
    }
}