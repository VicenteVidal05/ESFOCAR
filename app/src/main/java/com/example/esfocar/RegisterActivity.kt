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
import com.example.esfocar.ui.RegisterScreen
import com.example.esfocar.ui.theme.ESFOCARTheme
import com.example.esfocar.viewmodel.RegisterViewModel

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = ProfesorRepository(applicationContext)

        setContent {
            val viewModel: RegisterViewModel = viewModel(
                factory = remember { RegisterViewModel.Factory(repository) }
            )

            ESFOCARTheme {
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    onRegisterSuccess = {
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