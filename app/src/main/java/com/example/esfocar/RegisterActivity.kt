
package com.example.esfocar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.esfocar.ui.theme.ESFOCARTheme
import android.widget.Toast
class RegisterActivity : ComponentActivity() {

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
                        var nombre by remember { mutableStateOf("") }
                        var apellidos by remember { mutableStateOf("") }
                        var correo by remember { mutableStateOf("") }
                        Text("ESFOCAR Registro")
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(value = nombre, onValueChange = {nombre = it }, label={Text("Nombre")})
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = apellidos, onValueChange = {apellidos = it }, label={Text("Apellidos")})
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = correo, onValueChange = {correo = it }, label={Text("Correo")})
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
                        Text(text="¿Ya tienes una cuenta? Inicia Sesión", color = Color.Cyan,
                            modifier = Modifier.clickable{finish()})
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            if (
                                nombre.isNotBlank() &&
                                apellidos.isNotBlank() &&
                                correo.isNotBlank() &&
                                contrasena.isNotBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Registrado!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }else{
                                Toast.makeText(
                                    context,
                                    "Por favor, completa todos los campos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }) {Text("Registrate") }
                    }
                }
            }
        }
    }
}
