package com.uade.vigia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MissionEntity::class, EventEntity::class], version = 1, exportSchema = false)
abstract class VigiaDatabase : RoomDatabase() {
    abstract fun missionDao(): MissionDao

    companion object {
        @Volatile
        private var INSTANCE: VigiaDatabase? = null

        fun getDatabase(context: Context): VigiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VigiaDatabase::class.java,
                    "vigia_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
