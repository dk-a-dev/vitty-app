package com.dscvit.vitty.data.converter

import androidx.room.TypeConverter
import com.dscvit.vitty.ui.coursepage.models.NoteType

class Converters {
    @TypeConverter
    fun fromNoteType(noteType: NoteType): String = noteType.name

    @TypeConverter
    fun toNoteType(noteType: String): NoteType = NoteType.valueOf(noteType)
}
