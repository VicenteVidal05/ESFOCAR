package com.example.esfocar.data.local

import com.example.esfocar.model.ItemRubrica
import com.example.esfocar.model.NivelLogro
import org.json.JSONArray
import org.json.JSONObject

object RubricaSerializador {
    fun toJson(items: List<ItemRubrica>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("criterio", item.criterio)
                put("pNoLogrado", item.puntajeNoLogrado)
                put("pMedLogrado", item.puntajeMedianamenteLogrado)
                put("pLogrado", item.puntajeLogrado)
                put("nivel", item.nivelSeleccionado?.name ?: "")
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(json: String): List<ItemRubrica> {
        if (json.isBlank()) return emptyList()
        val lista = mutableListOf<ItemRubrica>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val nivelStr = obj.optString("nivel", "")
            val nivel = NivelLogro.entries.find { it.name == nivelStr }
            lista.add(
                ItemRubrica(
                    id = obj.optLong("id", i.toLong()),
                    criterio = obj.optString("criterio", ""),
                    puntajeNoLogrado = obj.optDouble("pNoLogrado", 0.0),
                    puntajeMedianamenteLogrado = obj.optDouble("pMedLogrado", 1.0),
                    puntajeLogrado = obj.optDouble("pLogrado", 2.0),
                    nivelSeleccionado = nivel
                )
            )
        }
        return lista
    }
}