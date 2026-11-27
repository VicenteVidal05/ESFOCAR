package com.example.esfocar.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FeriadoExternoDto(
    @SerializedName("date")
    val fecha: String,

    @SerializedName("localName")
    val nombreLocal: String,

    @SerializedName("name")
    val nombreInternacional: String
)