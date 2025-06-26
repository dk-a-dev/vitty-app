package com.dscvit.vitty.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dscvit.vitty.data.converter.Converters
import com.dscvit.vitty.data.dao.NoteDao
import com.dscvit.vitty.data.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class VittyDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var _instance: VittyDatabase? = null

        fun getDatabase(context: Context): VittyDatabase =
            _instance ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            VittyDatabase::class.java,
                            "vitty_database",
                        ).build()
                _instance = instance
                instance
            }
    }
}
