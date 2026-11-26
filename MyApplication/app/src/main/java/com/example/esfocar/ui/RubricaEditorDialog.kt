package com.example.esfocar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.esfocar.data.local.RubricaSerializador
import com.example.esfocar.model.EvaluacionRubrica
import com.example.esfocar.model.ItemRubrica
import com.example.esfocar.model.NivelLogro
import com.example.esfocar.model.PlantillaRubrica
import com.example.esfocar.util.RubricaPdfHelper
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantillaRubricaDialog(
    plantillaExistente: PlantillaRubrica?,
    onDismiss: () -> Unit,
    onGuardar: (titulo: String, items: List<ItemRubrica>) -> Unit
) {
    val bgDark = Color(0xFF0E1310)
    val surfaceDark = Color(0xFF161D19)
    val bordeSutil = Color(0xFF28352E)
    val verdeAccent = Color(0xFF4CAF7D)
    val doradoDetalle = Color(0xFFC5A028)
    val textoSecundario = Color(0xFF94A39B)

    var titulo by remember { mutableStateOf(plantillaExistente?.titulo ?: "") }
    val items = remember {
        mutableStateListOf<ItemRubrica>().apply {
            val cargados = plantillaExistente?.let { RubricaSerializador.fromJson(it.itemsJson) }
            if (!cargados.isNullOrEmpty()) addAll(cargados) else add(ItemRubrica(id = 1L))
        }
    }

    val puntajeMaximoTotal = items.sumOf { it.puntajeMaximo() }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = verdeAccent,
        unfocusedBorderColor = bordeSutil,
        focusedLabelColor = verdeAccent,
        unfocusedLabelColor = textoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = verdeAccent,
        focusedContainerColor = surfaceDark,
        unfocusedContainerColor = surfaceDark
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = bgDark,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (plantillaExistente == null) "Diseñar Rúbrica" else "Editar Rúbrica",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = { onGuardar(titulo, items.toList()) }) {
                            Text("Guardar", color = verdeAccent, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    color = surfaceDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, doradoDetalle.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${items.size} criterio(s) configurados",
                            color = textoSecundario,
                            fontSize = 13.sp
                        )
                        Text(
                            text = String.format(Locale.US, "Puntaje Máx: %.2f pts", puntajeMaximoTotal),
                            color = doradoDetalle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text("Título de la rúbrica / evaluación") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                        ItemPlantillaCard(
                            numero = index + 1,
                            item = item,
                            puedeEliminar = items.size > 1,
                            surfaceColor = surfaceDark,
                            borderColor = bordeSutil,
                            verdeAccent = verdeAccent,
                            textoSecundario = textoSecundario,
                            fieldColors = fieldColors,
                            onUpdate = { items[index] = it },
                            onDelete = { items.removeAt(index) }
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = { items.add(ItemRubrica(id = System.currentTimeMillis() + items.size)) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, verdeAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = verdeAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Añadir ítem", color = verdeAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemPlantillaCard(
    numero: Int,
    item: ItemRubrica,
    puedeEliminar: Boolean,
    surfaceColor: Color,
    borderColor: Color,
    verdeAccent: Color,
    textoSecundario: Color,
    fieldColors: TextFieldColors,
    onUpdate: (ItemRubrica) -> Unit,
    onDelete: () -> Unit
) {
    var txtNoLogrado by remember(item.id) { mutableStateOf(item.puntajeNoLogrado.toString()) }
    var txtMedLogrado by remember(item.id) { mutableStateOf(item.puntajeMedianamenteLogrado.toString()) }
    var txtLogrado by remember(item.id) { mutableStateOf(item.puntajeLogrado.toString()) }

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ítem #$numero", color = verdeAccent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (puedeEliminar) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Quitar", tint = Color(0xFFE57373))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = item.criterio,
                onValueChange = { onUpdate(item.copy(criterio = it)) },
                label = { Text("Descripción del criterio") },
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Puntaje asignado a cada nivel:", fontSize = 12.sp, color = textoSecundario)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = txtNoLogrado,
                    onValueChange = {
                        txtNoLogrado = it
                        onUpdate(item.copy(puntajeNoLogrado = it.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("No Logrado", fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = txtMedLogrado,
                    onValueChange = {
                        txtMedLogrado = it
                        onUpdate(item.copy(puntajeMedianamenteLogrado = it.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Med. Log.", fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = txtLogrado,
                    onValueChange = {
                        txtLogrado = it
                        onUpdate(item.copy(puntajeLogrado = it.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Logrado", fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluarAlumnoDialog(
    plantilla: PlantillaRubrica?,
    evaluacionExistente: EvaluacionRubrica?,
    onDismiss: () -> Unit,
    onGuardarEvaluacion: (
        plantillaId: Int,
        tituloRubrica: String,
        nombreEstudiante: String,
        itemsEvaluados: List<ItemRubrica>
    ) -> Unit
) {
    val bgDark = Color(0xFF0E1310)
    val surfaceDark = Color(0xFF161D19)
    val bordeSutil = Color(0xFF28352E)
    val verdePrimario = Color(0xFF0B4F32)
    val verdeAccent = Color(0xFF4CAF7D)
    val doradoDetalle = Color(0xFFC5A028)
    val textoSecundario = Color(0xFF94A39B)

    val tituloRubrica = evaluacionExistente?.tituloRubrica ?: plantilla?.titulo.orEmpty()
    val plantillaId = evaluacionExistente?.plantillaId ?: plantilla?.id ?: 0

    var nombreEstudiante by remember {
        mutableStateOf(evaluacionExistente?.nombreEstudiante ?: "")
    }
    val items = remember {
        mutableStateListOf<ItemRubrica>().apply {
            val json = evaluacionExistente?.itemsJson ?: plantilla?.itemsJson.orEmpty()
            addAll(RubricaSerializador.fromJson(json))
        }
    }

    val puntajeObtenido = items.sumOf { it.puntajeObtenido() }
    val puntajeMaximo = items.sumOf { it.puntajeMaximo() }
    val notaCalculada = RubricaPdfHelper.calcularNotaChilena(puntajeObtenido, puntajeMaximo)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = verdeAccent,
        unfocusedBorderColor = bordeSutil,
        focusedLabelColor = verdeAccent,
        unfocusedLabelColor = textoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = verdeAccent,
        focusedContainerColor = surfaceDark,
        unfocusedContainerColor = surfaceDark
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = bgDark,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (evaluacionExistente == null) "Evaluar Alumno" else "Editar Evaluación",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(text = tituloRubrica, color = textoSecundario, fontSize = 12.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                onGuardarEvaluacion(plantillaId, tituloRubrica, nombreEstudiante, items.toList())
                            }
                        ) {
                            Text("Finalizar PDF", color = verdeAccent, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Nota chilena en vivo (1.00 - 7.00)
                Surface(
                    color = surfaceDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, doradoDetalle.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Escala Chile (60% exigencia)", fontSize = 12.sp, color = textoSecundario)
                            Text(
                                text = String.format(Locale.US, "Puntaje: %.2f / %.2f pts", puntajeObtenido, puntajeMaximo),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("NOTA", fontSize = 11.sp, color = doradoDetalle)
                            Text(
                                text = String.format(Locale.US, "%.2f", notaCalculada),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (notaCalculada >= 4.0) verdeAccent else Color(0xFFE57373)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = nombreEstudiante,
                            onValueChange = { nombreEstudiante = it },
                            label = { Text("Nombre completo del alumno a evaluar") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                        Surface(
                            color = surfaceDark,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, bordeSutil)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${index + 1}. ${item.criterio}",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    NivelLogro.entries.forEach { nivel ->
                                        val seleccionado = item.nivelSeleccionado == nivel
                                        val pts = when (nivel) {
                                            NivelLogro.NO_LOGRADO -> item.puntajeNoLogrado
                                            NivelLogro.MEDIANAMENTE_LOGRADO -> item.puntajeMedianamenteLogrado
                                            NivelLogro.LOGRADO -> item.puntajeLogrado
                                        }
                                        val etiquetaCorta = when (nivel) {
                                            NivelLogro.NO_LOGRADO -> "No Log. (${pts})"
                                            NivelLogro.MEDIANAMENTE_LOGRADO -> "Med. (${pts})"
                                            NivelLogro.LOGRADO -> "Logrado (${pts})"
                                        }

                                        FilterChip(
                                            selected = seleccionado,
                                            onClick = { items[index] = item.copy(nivelSeleccionado = nivel) },
                                            label = {
                                                Text(
                                                    text = etiquetaCorta,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = verdePrimario,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color(0xFF0E1310),
                                                labelColor = textoSecundario
                                            ),
                                            modifier = Modifier.weight(1f)
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