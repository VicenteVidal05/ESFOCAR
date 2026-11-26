package com.example.esfocar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profesor")
data class ProfesorEntity(
    @PrimaryKey
    @ColumnInfo(name = "correo")
    val correo: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "apellidos")
    val apellidos: String
)