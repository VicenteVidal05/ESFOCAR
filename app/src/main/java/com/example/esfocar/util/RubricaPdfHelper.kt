package com.example.esfocar.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.esfocar.model.ItemRubrica
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object RubricaPdfHelper {

    fun calcularNotaChilena(
        puntajeObtenido: Double,
        puntajeMaximo: Double,
        exigencia: Double = 0.60
    ): Double {
        if (puntajeMaximo <= 0.0) return 1.00
        val p = puntajeObtenido.coerceIn(0.0, puntajeMaximo)
        val corte = puntajeMaximo * exigencia

        val notaRaw = if (p < corte) {
            if (corte <= 0.0) 4.0 else 1.0 + 3.0 * (p / corte)
        } else {
            val tramoSuperior = puntajeMaximo - corte
            if (tramoSuperior <= 0.0) 7.0 else 4.0 + 3.0 * ((p - corte) / tramoSuperior)
        }

        val notaAcotada = notaRaw.coerceIn(1.0, 7.0)
        return (notaAcotada * 100.0).roundToInt() / 100.0
    }

    fun generarPdfRubrica(
        context: Context,
        tituloRubrica: String,
        nombreEstudiante: String,
        docente: String,
        items: List<ItemRubrica>,
        puntajeObtenido: Double,
        puntajeMaximo: Double,
        notaFinal: Double,
        rutaAnterior: String? = null
    ): String {
        if (!rutaAnterior.isNullOrBlank()) {
            runCatching { File(rutaAnterior).delete() }
        }

        val carpeta = File(context.filesDir, "rubricas").apply { if (!exists()) mkdirs() }
        val nombreLimpio = nombreEstudiante.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val archivoPdf = File(carpeta, "Rubrica_${nombreLimpio}_${System.currentTimeMillis()}.pdf")

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paintHeaderBg = Paint().apply { color = Color.rgb(11, 79, 50) }
        val paintGoldLine = Paint().apply { color = Color.rgb(197, 160, 40) }
        val paintCardBg = Paint().apply { color = Color.rgb(244, 247, 245) }
        val paintBorder = Paint().apply {
            color = Color.rgb(200, 212, 205)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val paintTitleWhite = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSubWhite = Paint().apply {
            color = Color.rgb(210, 232, 220)
            textSize = 11f
        }
        val paintBold = Paint().apply {
            color = Color.rgb(20, 30, 25)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintRegular = Paint().apply {
            color = Color.rgb(45, 55, 50)
            textSize = 11f
        }
        val paintNota = Paint().apply {
            color = if (notaFinal >= 4.0) Color.rgb(11, 79, 50) else Color.rgb(180, 35, 35)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 82f, paintHeaderBg)
        canvas.drawRect(0f, 82f, pageWidth.toFloat(), 86f, paintGoldLine)
        canvas.drawText("ESFOCAR · ACTA DE EVALUACIÓN POR RÚBRICA", 36f, 36f, paintTitleWhite)
        val fechaStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Fecha: $fechaStr   |   Evaluador: $docente", 36f, 60f, paintSubWhite)

        var y = 108f
        canvas.drawRect(36f, y, pageWidth - 36f, y + 78f, paintCardBg)
        canvas.drawRect(36f, y, pageWidth - 36f, y + 78f, paintBorder)

        canvas.drawText("Rúbrica: $tituloRubrica", 48f, y + 22f, paintBold)
        canvas.drawText("Estudiante: $nombreEstudiante", 48f, y + 42f, paintRegular)
        canvas.drawText(
            String.format(Locale.US, "Puntaje Total: %.2f / %.2f pts (Exigencia 60%%)", puntajeObtenido, puntajeMaximo),
            48f,
            y + 62f,
            paintRegular
        )

        canvas.drawText("NOTA FINAL", pageWidth - 135f, y + 25f, paintRegular)
        canvas.drawText(String.format(Locale.US, "%.2f", notaFinal), pageWidth - 130f, y + 58f, paintNota)

        y += 102f
        canvas.drawText("DETALLE DE CRITERIOS EVALUADOS", 36f, y, paintBold)
        y += 14f

        items.forEachIndexed { index, item ->
            if (y > pageHeight - 95f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 48f
            }

            val boxHeight = 64f
            canvas.drawRect(36f, y, pageWidth - 36f, y + boxHeight, paintCardBg)
            canvas.drawRect(36f, y, pageWidth - 36f, y + boxHeight, paintBorder)

            val tituloItem = "${index + 1}. ${item.criterio.ifBlank { "Criterio sin nombre" }}"
            val tituloRecortado = if (tituloItem.length > 62) tituloItem.take(59) + "..." else tituloItem
            canvas.drawText(tituloRecortado, 48f, y + 20f, paintBold)

            val estado = item.nivelSeleccionado?.etiqueta ?: "Sin evaluar"
            val ptsObtenidos = item.puntajeObtenido()
            val ptsMaxItem = item.puntajeMaximo()

            canvas.drawText("Indicador alcanzado: $estado", 48f, y + 39f, paintRegular)
            canvas.drawText(
                String.format(
                    Locale.US,
                    "Escala ítem -> No Logrado: %.1f | Med. Logrado: %.1f | Logrado: %.1f",
                    item.puntajeNoLogrado,
                    item.puntajeMedianamenteLogrado,
                    item.puntajeLogrado
                ),
                48f,
                y + 54f,
                paintRegular
            )

            canvas.drawText(
                String.format(Locale.US, "%.2f / %.2f pts", ptsObtenidos, ptsMaxItem),
                pageWidth - 125f,
                y + 36f,
                paintBold
            )

            y += boxHeight + 10f
        }

        pdfDocument.finishPage(page)
        FileOutputStream(archivoPdf).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return archivoPdf.absolutePath
    }
}