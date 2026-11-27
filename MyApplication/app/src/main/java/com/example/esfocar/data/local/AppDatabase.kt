package com.example.esfocar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.esfocar.data.local.dao.DocumentoDao
import com.example.esfocar.data.local.dao.ProfesorDao
import com.example.esfocar.data.local.dao.RubricaDao
import com.example.esfocar.data.local.entity.DocumentoEntity
import com.example.esfocar.data.local.entity.EvaluacionRubricaEntity
import com.example.esfocar.data.local.entity.PlantillaRubricaEntity
import com.example.esfocar.data.local.entity.ProfesorEntity

@Database(
    entities = [
        ProfesorEntity::class,
        DocumentoEntity::class,
        PlantillaRubricaEntity::class,
        EvaluacionRubricaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profesorDao(): ProfesorDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun rubricaDao(): RubricaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esfocar_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}