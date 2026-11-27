package com.example.esfocar.model

enum class NivelLogro(val etiqueta: String) {
    NO_LOGRADO("No Logrado"),
    MEDIANAMENTE_LOGRADO("Medianamente Logrado"),
    LOGRADO("Logrado")
}

data class ItemRubrica(
    val id: Long = System.currentTimeMillis(),
    val criterio: String = "",
    val puntajeNoLogrado: Double = 0.0,
    val puntajeMedianamenteLogrado: Double = 1.0,
    val puntajeLogrado: Double = 2.0,
    val nivelSeleccionado: NivelLogro? = null
) {
    fun puntajeObtenido(): Double = when (nivelSeleccionado) {
        NivelLogro.NO_LOGRADO -> puntajeNoLogrado
        NivelLogro.MEDIANAMENTE_LOGRADO -> puntajeMedianamenteLogrado
        NivelLogro.LOGRADO -> puntajeLogrado
        null -> 0.0
    }

    fun puntajeMaximo(): Double =
        maxOf(puntajeNoLogrado, puntajeMedianamenteLogrado, puntajeLogrado)
}

data class PlantillaRubrica(
    val id: Int = 0,
    val profesorCorreo: String,
    val titulo: String,
    val itemsJson: String,
    val puntajeMaximo: Double,
    val exigencia: Double = 0.60,
    val fechaCreacion: Long = System.currentTimeMillis()
)

data class EvaluacionRubrica(
    val id: Int = 0,
    val plantillaId: Int,
    val profesorCorreo: String,
    val tituloRubrica: String,
    val nombreEstudiante: String,
    val itemsJson: String,
    val puntajeObtenido: Double,
    val puntajeMaximo: Double,
    val exigencia: Double = 0.60,
    val notaFinal: Double,
    val rutaPdf: String? = null,
    val fechaEvaluacion: Long = System.currentTimeMillis()
)