package com.example.esfocar.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.esfocar.model.Documento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerDialog(
    documento: Documento,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var paginas by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorLectura by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(documento.rutaOContenido) {
        cargando = true
        errorLectura = null
        withContext(Dispatchers.IO) {
            try {
                val pfd: ParcelFileDescriptor? = if (documento.rutaOContenido.startsWith("/")) {
                    ParcelFileDescriptor.open(
                        File(documento.rutaOContenido),
                        ParcelFileDescriptor.MODE_READ_ONLY
                    )
                } else {
                    context.contentResolver.openFileDescriptor(
                        Uri.parse(documento.rutaOContenido),
                        "r"
                    )
                }

                if (pfd == null) {
                    errorLectura = "No se encontró el archivo PDF."
                } else {
                    pfd.use { descriptor ->
                        PdfRenderer(descriptor).use { renderer ->
                            val listaBitmaps = mutableListOf<Bitmap>()
                            for (i in 0 until renderer.pageCount) {
                                renderer.openPage(i).use { page ->
                                    // Escala 2x para que el texto se lea nítido
                                    val width = page.width * 2
                                    val height = page.height * 2
                                    val bitmap = Bitmap.createBitmap(
                                        width,
                                        height,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    // Fondo blanco por si el PDF tiene transparencia
                                    bitmap.eraseColor(android.graphics.Color.WHITE)
                                    page.render(
                                        bitmap,
                                        null,
                                        null,
                                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                    )
                                    listaBitmaps.add(bitmap)
                                }
                            }
                            paginas = listaBitmaps
                        }
                    }
                }
            } catch (e: Exception) {
                errorLectura = "Error al leer el PDF: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = documento.titulo,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (paginas.isNotEmpty()) {
                                Text(
                                    text = "${paginas.size} página(s)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar visor"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF2B2B2B)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    cargando -> {
                        CircularProgressIndicator(color = Color.White)
                    }
                    errorLectura != null -> {
                        Text(
                            text = errorLectura!!,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    paginas.isEmpty() -> {
                        Text(
                            text = "El documento PDF está vacío.",
                            color = Color.White
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(paginas) { index, bitmap ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Página ${index + 1}",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                        Text(
                                            text = "Página ${index + 1} de ${paginas.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier
                                                .align(Alignment.End)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}