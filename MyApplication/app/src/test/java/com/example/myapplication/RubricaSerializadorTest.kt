package com.example.myapplication

import com.example.esfocar.data.local.RubricaSerializador
import com.example.esfocar.model.ItemRubrica
import com.example.esfocar.model.NivelLogro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RubricaSerializadorTest {

    @Test
    fun `serializar y deserializar lista de items conserva criterios puntajes y niveles`() {
        val originales = listOf(
            ItemRubrica(
                id = 10L,
                criterio = "Dominio escénico",
                puntajeNoLogrado = 0.0,
                puntajeMedianamenteLogrado = 2.0,
                puntajeLogrado = 4.0,
                nivelSeleccionado = NivelLogro.LOGRADO
            ),
            ItemRubrica(
                id = 20L,
                criterio = "Claridad vocal",
                puntajeNoLogrado = 0.0,
                puntajeMedianamenteLogrado = 1.0,
                puntajeLogrado = 2.0,
                nivelSeleccionado = null
            )
        )

        val json = RubricaSerializador.toJson(originales)
        val reconstruidos = RubricaSerializador.fromJson(json)

        assertEquals(2, reconstruidos.size)
        assertEquals("Dominio escénico", reconstruidos[0].criterio)
        assertEquals(4.0, reconstruidos[0].puntajeLogrado, 0.01)
        assertEquals(NivelLogro.LOGRADO, reconstruidos[0].nivelSeleccionado)
        assertNull(reconstruidos[1].nivelSeleccionado)
    }

    @Test
    fun `fromJson con cadena vacia retorna lista vacia`() {
        val resultado = RubricaSerializador.fromJson("")
        assertTrue(resultado.isEmpty())
    }
}