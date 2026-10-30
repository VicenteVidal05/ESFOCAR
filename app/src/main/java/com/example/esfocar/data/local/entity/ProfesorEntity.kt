package com.example.esfocar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profesor")
data class ProfesorEntity(
    @PrimaryKey
    @ColumnInfo(name = "correo")
    val correo: String,

    /**
     * Se guarda la contraseña en texto plano de momento,
     * pero en un futuro se encriptará,
     * aqui ira guardado el hash de la contraseña
     */

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "apellidos")
    val apellidos: String
)