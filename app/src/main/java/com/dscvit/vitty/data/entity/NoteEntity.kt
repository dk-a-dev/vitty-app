package com.dscvit.vitty.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dscvit.vitty.ui.coursepage.models.NoteType

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: String,
    val title: String,
    val content: String,
    val type: NoteType,
    val isStarred: Boolean = false,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
