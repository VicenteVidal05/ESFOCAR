package com.example.esfocar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.esfocar.model.EvaluacionRubrica
import com.example.esfocar.model.PlantillaRubrica

@Entity(tableName = "plantillas_rubrica")
data class PlantillaRubricaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profesorCorreo: String,
    val titulo: String,
    val itemsJson: String,
    val puntajeMaximo: Double,
    val exigencia: Double = 0.60,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    fun toModel() = PlantillaRubrica(
        id = id,
        profesorCorreo = profesorCorreo,
        titulo = titulo,
        itemsJson = itemsJson,
        puntajeMaximo = puntajeMaximo,
        exigencia = exigencia,
        fechaCreacion = fechaCreacion
    )

    companion object {
        fun fromModel(p: PlantillaRubrica) = PlantillaRubricaEntity(
            id = p.id,
            profesorCorreo = p.profesorCorreo,
            titulo = p.titulo,
            itemsJson = p.itemsJson,
            puntajeMaximo = p.puntajeMaximo,
            exigencia = p.exigencia,
            fechaCreacion = p.fechaCreacion
        )
    }
}

@Entity(tableName = "evaluaciones_rubrica")
data class EvaluacionRubricaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantillaId: Int,
    val profesorCorreo: String,
    val tituloRubrica: String,
    val nombreEstudiante: String,
    val itemsJson: String,
    val puntajeObtenido: Double,
    val puntajeMaximo: Double,
    val exigencia: Double = 0.60,
    val notaFinal: Double,
    val rutaPdf: String,
    val fechaEvaluacion: Long = System.currentTimeMillis()
) {
    fun toModel() = EvaluacionRubrica(
        id = id,
        plantillaId = plantillaId,
        profesorCorreo = profesorCorreo,
        tituloRubrica = tituloRubrica,
        nombreEstudiante = nombreEstudiante,
        itemsJson = itemsJson,
        puntajeObtenido = puntajeObtenido,
        puntajeMaximo = puntajeMaximo,
        exigencia = exigencia,
        notaFinal = notaFinal,
        rutaPdf = rutaPdf,
        fechaEvaluacion = fechaEvaluacion
    )

    companion object {
        fun fromModel(e: EvaluacionRubrica) = EvaluacionRubricaEntity(
            id = e.id,
            plantillaId = e.plantillaId,
            profesorCorreo = e.profesorCorreo,
            tituloRubrica = e.tituloRubrica,
            nombreEstudiante = e.nombreEstudiante,
            itemsJson = e.itemsJson,
            puntajeObtenido = e.puntajeObtenido,
            puntajeMaximo = e.puntajeMaximo,
            exigencia = e.exigencia,
            notaFinal = e.notaFinal,
            rutaPdf = e.rutaPdf,
            fechaEvaluacion = e.fechaEvaluacion
        )
    }
}