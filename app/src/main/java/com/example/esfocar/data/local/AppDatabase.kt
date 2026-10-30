package com.example.esfocar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.esfocar.data.local.dao.ProfesorDao
import com.example.esfocar.data.local.dao.DocumentoDao
import com.example.esfocar.data.local.entity.ProfesorEntity
import com.example.esfocar.data.local.entity.DocumentoEntity

@Database(
    entities = [ProfesorEntity::class, DocumentoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profesorDao(): ProfesorDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "evaluacion_docente_db"
                )
                // en prototipos es seguro usar fallbackToDestructiveMigration
                // para que Room limpie y rehaga las tablas por si cambia el esquema
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instancia
                instancia
            }
        }
    }
}