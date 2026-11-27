package com.example.myapplication

import com.example.esfocar.model.ItemRubrica
import com.example.esfocar.model.NivelLogro
import com.example.esfocar.util.RubricaPdfHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class RubricaPdfHelperTest {

    @Test
    fun `puntaje cero retorna nota minima 1_0`() {
        val nota = RubricaPdfHelper.calcularNotaChilena(puntajeObtenido = 0.0, puntajeMaximo = 20.0)
        assertEquals(1.0, nota, 0.01)
    }

    @Test
    fun `puntaje de corte al 60 porciento retorna nota 4_0`() {
        val nota = RubricaPdfHelper.calcularNotaChilena(puntajeObtenido = 12.0, puntajeMaximo = 20.0)
        assertEquals(4.0, nota, 0.01)
    }

    @Test
    fun `puntaje maximo retorna nota 7_0`() {
        val nota = RubricaPdfHelper.calcularNotaChilena(puntajeObtenido = 20.0, puntajeMaximo = 20.0)
        assertEquals(7.0, nota, 0.01)
    }

    @Test
    fun `puntaje intermedio bajo el corte calcula nota reprobatoria proporcional`() {
        val nota = RubricaPdfHelper.calcularNotaChilena(puntajeObtenido = 6.0, puntajeMaximo = 20.0)
        assertEquals(2.5, nota, 0.01)
    }

    @Test
    fun `puntaje maximo cero o negativo retorna 1_0 sin division por cero`() {
        val nota = RubricaPdfHelper.calcularNotaChilena(puntajeObtenido = 5.0, puntajeMaximo = 0.0)
        assertEquals(1.0, nota, 0.01)
    }

    @Test
    fun `itemRubrica calcula correctamente puntaje obtenido y maximo segun nivel`() {
        val itemSinEvaluar = ItemRubrica(criterio = "Postura", puntajeNoLogrado = 0.0, puntajeMedianamenteLogrado = 1.5, puntajeLogrado = 3.0)
        assertEquals(0.0, itemSinEvaluar.puntajeObtenido(), 0.01)
        assertEquals(3.0, itemSinEvaluar.puntajeMaximo(), 0.01)

        val itemMediano = itemSinEvaluar.copy(nivelSeleccionado = NivelLogro.MEDIANAMENTE_LOGRADO)
        assertEquals(1.5, itemMediano.puntajeObtenido(), 0.01)

        val itemLogrado = itemSinEvaluar.copy(nivelSeleccionado = NivelLogro.LOGRADO)
        assertEquals(3.0, itemLogrado.puntajeObtenido(), 0.01)
    }
}