package com.example.esfocar

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocar.data.ProfesorRepository
import com.example.esfocar.ui.LoginScreen
import com.example.esfocar.ui.theme.ESFOCARTheme
import com.example.esfocar.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = ProfesorRepository(applicationContext)
        if (repository.haySesionActiva()) {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
            return
        }

        setContent {
            val viewModel: LoginViewModel = viewModel(
                factory = remember { LoginViewModel.Factory(repository) }
            )

            ESFOCARTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = {
                        val opciones = ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.entrada_bounce,
                            android.R.anim.fade_out
                        )
                        startActivity(
                            Intent(this, RegisterActivity::class.java),
                            opciones.toBundle()
                        )
                    },
                    onLoginSuccess = {
                        val opciones = ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.entrada_bounce,
                            android.R.anim.fade_out
                        )
                        val intent = Intent(this, MenuActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent, opciones.toBundle())
                    }
                )
            }
        }
    }
}