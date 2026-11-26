package com.example.esfocar

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocar.data.DocumentoRepository
import com.example.esfocar.data.ProfesorRepository
import com.example.esfocar.ui.MenuScreen
import com.example.esfocar.ui.theme.ESFOCARTheme
import com.example.esfocar.viewmodel.MenuViewModel

class MenuActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val documentoRepository = DocumentoRepository(applicationContext)
        val profesorRepository = ProfesorRepository(applicationContext)

        setContent {
            val viewModel: MenuViewModel = viewModel(
                factory = remember {
                    MenuViewModel.Factory(documentoRepository, profesorRepository)
                }
            )

            ESFOCARTheme {
                MenuScreen(
                    viewModel = viewModel,
                    onLogout = {
                        val opciones = ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.entrada_bounce,
                            android.R.anim.fade_out
                        )
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent, opciones.toBundle())
                    }
                )
            }
        }
    }
}