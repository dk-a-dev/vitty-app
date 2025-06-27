package com.dscvit.vitty.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dscvit.vitty.data.converter.Converters
import com.dscvit.vitty.data.dao.NoteDao
import com.dscvit.vitty.data.dao.ReminderDao
import com.dscvit.vitty.data.entity.NoteEntity
import com.dscvit.vitty.data.entity.ReminderEntity

@Database(
    entities = [NoteEntity::class, ReminderEntity::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class VittyDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao

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
                        )
                        .fallbackToDestructiveMigration(true)
                        .build()
                _instance = instance
                instance
            }
    }
}
