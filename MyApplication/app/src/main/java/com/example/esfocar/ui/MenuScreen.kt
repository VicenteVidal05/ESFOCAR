package com.example.esfocar.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esfocar.data.remote.dto.FeriadoExternoDto
import com.example.esfocar.model.Documento
import com.example.esfocar.model.EvaluacionRubrica
import com.example.esfocar.model.PlantillaRubrica
import com.example.esfocar.viewmodel.MenuViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale //FilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current


    var tabSeleccionada by remember { mutableIntStateOf(0) }
    var evaluacionADescargar by remember { mutableStateOf<EvaluacionRubrica?>(null) }

    val bgDark = Color(0xFF0E1310)
    val surfaceDark = Color(0xFF161D19)
    val bordeSutil = Color(0xFF28352E)
    val verdePrimario = Color(0xFF0B4F32)
    val verdeAccent = Color(0xFF4CAF7D)
    val doradoDetalle = Color(0xFFC5A028)
    val textoSecundario = Color(0xFF94A39B)

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

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val nombreReal = obtenerNombreArchivo(context, uri)
            val rutaLocal = copiarPdfAAlmacenamientoInterno(context, uri, nombreReal)
            if (rutaLocal != null) {
                viewModel.agregarDocumento(nombreReal, rutaLocal)
            } else {
                Toast.makeText(context, "No se pudo importar el PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val descargarPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinoUri ->
        val eval = evaluacionADescargar
        if (destinoUri != null && eval != null) {
            viewModel.exportarYRegistrarPdfEvaluacion(context, eval, destinoUri)
        }
        evaluacionADescargar = null
    }

    if (uiState.mostrarEditorPlantilla) {
        PlantillaRubricaDialog(
            plantillaExistente = uiState.plantillaEnEdicion,
            onDismiss = { viewModel.cerrarEditorPlantilla() },
            onGuardar = { titulo, items ->
                viewModel.guardarPlantilla(
                    plantillaExistente = uiState.plantillaEnEdicion,
                    titulo = titulo,
                    items = items
                )
            }
        )
    }


    if (uiState.plantillaParaEvaluar != null || uiState.evaluacionEnEdicion != null) {
        EvaluarAlumnoDialog(
            plantilla = uiState.plantillaParaEvaluar,
            evaluacionExistente = uiState.evaluacionEnEdicion,
            onDismiss = { viewModel.cerrarDialogoEvaluacion() },
            onGuardarEvaluacion = { plantillaId, tituloRubrica, nombreEstudiante, itemsEvaluados ->
                viewModel.guardarEvaluacion(
                    context = context,
                    plantillaId = plantillaId,
                    tituloRubrica = tituloRubrica,
                    nombreEstudiante = nombreEstudiante,
                    itemsEvaluados = itemsEvaluados,
                    evaluacionExistente = uiState.evaluacionEnEdicion
                )
            }
        )
    }


    uiState.documentoSeleccionado?.let { docSeleccionado ->
        PdfViewerDialog(
            documento = docSeleccionado,
            onDismiss = { viewModel.cerrarDocumento() }
        )
    }

    Scaffold(
        containerColor = bgDark,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "ESFOCAR",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color.White
                            )
                            if (uiState.nombreActivo.isNotBlank()) {
                                Text(
                                    text = uiState.nombreActivo,
                                    fontSize = 12.sp,
                                    color = textoSecundario
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.cerrarSesion()
                                onLogout()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = textoSecundario
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
                )
                HorizontalDivider(thickness = 1.dp, color = bordeSutil)
            }
        },
        floatingActionButton = {
            if (tabSeleccionada == 0 || tabSeleccionada == 2) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (tabSeleccionada == 0) {
                            viewModel.abrirNuevaPlantilla()
                        } else {
                            pdfPicker.launch(arrayOf("application/pdf"))
                        }
                    },
                    containerColor = verdePrimario,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    icon = {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                    },
                    text = {
                        Text(
                            text = if (tabSeleccionada == 0) "Crear Rúbrica" else "Cargar PDF",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .fillMaxSize()
        ) {
            if (uiState.feriadosExternos.isNotEmpty()) {
                FeriadosExternosBanner(
                    feriados = uiState.feriadosExternos,
                    cargando = uiState.cargandoApiExterna,
                    surfaceColor = surfaceDark,
                    borderColor = bordeSutil,
                    doradoColor = doradoDetalle,
                    textoSecundario = textoSecundario
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tabSeleccionada == 0,
                    onClick = { tabSeleccionada = 0 },
                    label = { Text("Rúbricas (${uiState.plantillas.size})", fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = verdePrimario,
                        selectedLabelColor = Color.White,
                        containerColor = surfaceDark,
                        labelColor = textoSecundario
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = tabSeleccionada == 1,
                    onClick = { tabSeleccionada = 1 },
                    label = { Text("Notas (${uiState.evaluaciones.size})", fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = verdePrimario,
                        selectedLabelColor = Color.White,
                        containerColor = surfaceDark,
                        labelColor = textoSecundario
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = tabSeleccionada == 2,
                    onClick = { tabSeleccionada = 2 },
                    label = { Text("PDFs (${uiState.documentacionDocs.size})", fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = verdePrimario,
                        selectedLabelColor = Color.White,
                        containerColor = surfaceDark,
                        labelColor = textoSecundario
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(modifier = Modifier.width(28.dp), thickness = 2.dp, color = doradoDetalle)
            Spacer(modifier = Modifier.height(12.dp))

            when (tabSeleccionada) {
                0 -> {

                    if (uiState.plantillas.isEmpty()) {
                        EmptyStateBox(
                            icon = Icons.Outlined.Assignment,
                            titulo = "Sin rúbricas creadas",
                            subtitulo = "Pulsa \"Crear Rúbrica\" para definir los ítems y puntajes",
                            bordeSutil = bordeSutil,
                            textoSecundario = textoSecundario
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            items(uiState.plantillas, key = { it.id }) { plantilla ->
                                PlantillaCardDark(
                                    plantilla = plantilla,
                                    onEvaluar = { viewModel.iniciarEvaluacion(plantilla) },
                                    onEditar = { viewModel.abrirEditarPlantilla(plantilla) },
                                    onEliminar = { viewModel.eliminarPlantilla(plantilla) }
                                )
                            }
                        }
                    }
                }

                1 -> {

                    if (uiState.evaluaciones.isEmpty()) {
                        EmptyStateBox(
                            icon = Icons.Outlined.FactCheck,
                            titulo = "Aún no hay alumnos evaluados",
                            subtitulo = "Ve a \"Rúbricas\" y pulsa \"Evaluar\" en alguna rúbrica",
                            bordeSutil = bordeSutil,
                            textoSecundario = textoSecundario
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            items(uiState.evaluaciones, key = { it.id }) { eval ->
                                EvaluacionCardDark(
                                    evaluacion = eval,
                                    onVerPdf = { viewModel.verPdfEvaluacion(context, eval) },
                                    onDescargarPdf = {
                                        evaluacionADescargar = eval
                                        val nombreArchivo = "Rubrica_${eval.nombreEstudiante.replace(" ", "_")}.pdf"
                                        descargarPdfLauncher.launch(nombreArchivo)
                                    },
                                    onEditar = { viewModel.abrirEditarEvaluacion(eval) },
                                    onEliminar = { viewModel.eliminarEvaluacion(eval) }
                                )
                            }
                        }
                    }
                }

                2 -> {

                    if (uiState.documentacionDocs.isEmpty()) {
                        EmptyStateBox(
                            icon = Icons.Outlined.Description,
                            titulo = "Sin documentos cargados",
                            subtitulo = "Presiona \"Cargar PDF\" para añadir un archivo",
                            bordeSutil = bordeSutil,
                            textoSecundario = textoSecundario
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            items(uiState.documentacionDocs, key = { it.id }) { doc ->
                                DocumentoCardDark(
                                    documento = doc,
                                    surfaceColor = surfaceDark,
                                    borderColor = bordeSutil,
                                    accentColor = verdeAccent,
                                    secondaryTextColor = textoSecundario,
                                    onAbrir = { viewModel.abrirDocumento(doc) },
                                    onEliminar = { viewModel.eliminarDocumento(doc) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    bordeSutil: Color,
    textoSecundario: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = bordeSutil, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.85f))
            Text(subtitulo, fontSize = 13.sp, color = textoSecundario, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun PlantillaCardDark(
    plantilla: PlantillaRubrica,
    onEvaluar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val surfaceDark = Color(0xFF161D19)
    val bordeSutil = Color(0xFF28352E)
    val verdePrimario = Color(0xFF0B4F32)
    val verdeAccent = Color(0xFF4CAF7D)
    val doradoDetalle = Color(0xFFC5A028)
    val textoSecundario = Color(0xFF94A39B)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, bordeSutil)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plantilla.titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = String.format(Locale.US, "Puntaje máximo: %.2f pts", plantilla.puntajeMaximo),
                        fontSize = 12.sp,
                        color = textoSecundario
                    )
                }

                Button(
                    onClick = onEvaluar,
                    colors = ButtonDefaults.buttonColors(containerColor = verdePrimario),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Evaluar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = bordeSutil)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar rúbrica", color = doradoDetalle, fontSize = 12.sp)
                }
                TextButton(onClick = onEliminar) {
                    Text("Borrar", color = Color(0xFFE57373), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EvaluacionCardDark(
    evaluacion: EvaluacionRubrica,
    onVerPdf: () -> Unit,
    onDescargarPdf: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val surfaceDark = Color(0xFF161D19)
    val bordeSutil = Color(0xFF28352E)
    val verdeAccent = Color(0xFF4CAF7D)
    val doradoDetalle = Color(0xFFC5A028)
    val textoSecundario = Color(0xFF94A39B)

    val fechaFormateada = remember(evaluacion.fechaEvaluacion) {
        SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()).format(Date(evaluacion.fechaEvaluacion))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerPdf() },
        color = surfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, bordeSutil)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evaluacion.nombreEstudiante,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = evaluacion.tituloRubrica,
                        fontSize = 13.sp,
                        color = textoSecundario,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = Color(0xFF0E1310),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, doradoDetalle.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = String.format(Locale.US, "Nota %.2f", evaluacion.notaFinal),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (evaluacion.notaFinal >= 4.0) verdeAccent else Color(0xFFE57373),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = String.format(
                    Locale.US,
                    "Puntaje: %.2f / %.2f pts · %s",
                    evaluacion.puntajeObtenido,
                    evaluacion.puntajeMaximo,
                    fechaFormateada
                ),
                fontSize = 12.sp,
                color = textoSecundario
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = bordeSutil)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onVerPdf) {
                    Text("Ver PDF", color = verdeAccent, fontSize = 12.sp)
                }
                TextButton(onClick = onDescargarPdf) {
                    Text("Descargar", color = Color.White, fontSize = 12.sp)
                }
                TextButton(onClick = onEditar) {
                    Text("Editar", color = doradoDetalle, fontSize = 12.sp)
                }
                TextButton(onClick = onEliminar) {
                    Text("Borrar", color = Color(0xFFE57373), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DocumentoCardDark(
    documento: Documento,
    surfaceColor: Color,
    borderColor: Color,
    accentColor: Color,
    secondaryTextColor: Color,
    onAbrir: () -> Unit,
    onEliminar: () -> Unit
) {
    val fechaFormateada = remember(documento.fechaCarga) {
        SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()).format(Date(documento.fechaCarga))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAbrir() },
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = Color(0xFF0B4F32).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PictureAsPdf,
                        contentDescription = "PDF",
                        tint = accentColor,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = documento.titulo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = fechaFormateada, fontSize = 12.sp, color = secondaryTextColor)
                }
            }

            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Eliminar documento",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

private fun obtenerNombreArchivo(context: Context, uri: Uri): String {
    var nombre: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) nombre = cursor.getString(index)
            }
        }
    }
    return nombre ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Documento.pdf"
}

private fun copiarPdfAAlmacenamientoInterno(
    context: Context,
    uri: Uri,
    nombreArchivo: String
): String? {
    return try {
        val carpetaPdfs = File(context.filesDir, "pdfs").apply { if (!exists()) mkdirs() }
        val archivoDestino = File(carpetaPdfs, "${System.currentTimeMillis()}_$nombreArchivo")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(archivoDestino).use { output -> input.copyTo(output) }
        }
        archivoDestino.absolutePath
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun FeriadosExternosBanner(
    feriados: List<FeriadoExternoDto>,
    cargando: Boolean,
    surfaceColor: Color,
    borderColor: Color,
    doradoColor: Color,
    textoSecundario: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.EventAvailable,
                contentDescription = "Calendario académico",
                tint = doradoColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Próximos feriados Chile (API Externa)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = doradoColor
                )
                when {
                    cargando -> {
                        Text(
                            text = "Consultando calendario oficial...",
                            fontSize = 12.sp,
                            color = textoSecundario
                        )
                    }
                    feriados.isEmpty() -> {
                        Text(
                            text = "Sin conexión para consultar feriados",
                            fontSize = 12.sp,
                            color = textoSecundario
                        )
                    }
                    else -> {
                        feriados.forEach { feriado ->
                            Text(
                                text = "${feriado.fecha} · ${feriado.nombreLocal}",
                                fontSize = 12.sp,
                                color = textoSecundario,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}