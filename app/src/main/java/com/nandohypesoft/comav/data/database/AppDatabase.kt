package com.nandohypesoft.comav.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MusicaEntity::class, EscalaCultoEntity::class, EscalaMusicoDataEntity::class, EnsaioEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun musicaDao(): MusicaDao
    abstract fun escalaCultoDao(): EscalaCultoDao
    abstract fun escalaMusicoDataDao(): EscalaMusicoDataDao
    abstract fun ensaioDao(): EnsaioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "comav.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
