package com.dscvit.vitty.data.converter

import androidx.room.TypeConverter
import com.dscvit.vitty.ui.coursepage.NoteType

class Converters {
    
    @TypeConverter
    fun fromNoteType(noteType: NoteType): String {
        return noteType.name
    }
    
    @TypeConverter
    fun toNoteType(noteType: String): NoteType {
        return NoteType.valueOf(noteType)
    }
}
