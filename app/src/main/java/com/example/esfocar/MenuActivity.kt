package com.example.esfocar

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.esfocar.data.local.AppDatabase
import com.example.esfocar.data.local.entity.DocumentoEntity
import com.example.esfocar.data.session.SessionManager
import com.example.esfocar.ui.theme.ESFOCARTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ESFOCARTheme {
                AppScreen(onLogout = {
                    SessionManager(this).cerrarSesion()
                    // Animación de transición al cerrar sesión
                    val opciones = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.entrada_bounce,
                        android.R.anim.fade_out
                    )
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent, opciones.toBundle())
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val correoActivo = sessionManager.obtenerCorreoActivo() ?: ""
    val nombreActivo = sessionManager.obtenerNombreActivo() ?: ""

    val db = remember { AppDatabase.obtenerInstancia(context) }
    val documentosState = db.documentoDao()
        .obtenerPorProfesor(correoActivo)
        .collectAsState(initial = emptyList())

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && correoActivo.isNotBlank()) {
            // Tomar permisos persistentes de lectura del URI para poder abrirlo despues
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignorar si no soporta flags persistentes
            }

            // Obtener nombre del archivo si es posible
            val nombreDocumento = uri.lastPathSegment?.substringAfterLast('/') ?: "Documento.pdf"

            coroutineScope.launch {
                val nuevoDoc = DocumentoEntity(
                    titulo = nombreDocumento,
                    rutaOContenido = uri.toString(),
                    fechaCarga = System.currentTimeMillis(),
                    correoProfesor = correoActivo
                )
                db.documentoDao().insertar(nuevoDoc)
                Toast.makeText(context, "PDF agregado correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ESFOCAR", style = MaterialTheme.typography.titleMedium)
                            if (nombreActivo.isNotBlank()) {
                                Text(
                                    text = nombreActivo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.Filled.Logout,
                                contentDescription = "Cerrar sesión"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(
                        onClick = {
                            pdfPicker.launch(arrayOf("application/pdf"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Agregar PDF"
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Carpetas"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Mis Documentos (${documentosState.value.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (documentosState.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No has cargado ningún documento PDF aún.\nPulsa + abajo para añadir uno.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documentosState.value, key = { it.id }) { doc ->
                        DocumentoCard(
                            documento = doc,
                            onAbrir = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(doc.rutaOContenido), "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "No se pudo abrir el archivo PDF",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onEliminar = {
                                coroutineScope.launch {
                                    db.documentoDao().eliminar(doc)
                                    Toast.makeText(context, "Documento eliminado", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentoCard(
    documento: DocumentoEntity,
    onAbrir: () -> Unit,
    onEliminar: () -> Unit
) {
    val fechaFormateada = remember(documento.fechaCarga) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(documento.fechaCarga))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAbrir() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = "PDF",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = documento.titulo,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = fechaFormateada,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar documento",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}