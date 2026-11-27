package com.example.esfocar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.esfocar.data.DocumentoRepository
import com.example.esfocar.data.ProfesorRepository
import com.example.esfocar.data.RubricaRepository
import com.example.esfocar.ui.MenuScreen
import com.example.esfocar.ui.theme.ESFOCARTheme
import com.example.esfocar.viewmodel.MenuViewModel

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profesorRepository = ProfesorRepository(this)
        val documentoRepository = DocumentoRepository(this)
        val rubricaRepository = RubricaRepository(this)

        val factory = MenuViewModel.Factory(
            profesorRepository = profesorRepository,
            documentoRepository = documentoRepository,
            rubricaRepository = rubricaRepository
        )
        val viewModel = ViewModelProvider(this, factory)[MenuViewModel::class.java]

        setContent {
            ESFOCARTheme(darkTheme = true) {
                MenuScreen(
                    viewModel = viewModel,
                    onLogout = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
