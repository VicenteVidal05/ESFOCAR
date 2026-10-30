package com.example.esfocar

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
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ESFOCARTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        var contrasena by remember { mutableStateOf("") }
                        Text("ESFOCAR Iniciar sesión")
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(state = rememberTextFieldState(), label={Text("Correo")})
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
                        val context = LocalContext.current
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text="No tienes una cuenta? Registrate", color = Color.Cyan,
                            modifier = Modifier.clickable{context.startActivity(Intent(context,
                                RegisterActivity::class.java))})
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {context.startActivity(Intent(context, MenuActivity::class.java))}) {Text("Iniciar sesion") }
                    }
                }
            }
        }
    }
}