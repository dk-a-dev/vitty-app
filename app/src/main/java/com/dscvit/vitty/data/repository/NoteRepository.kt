package com.dscvit.vitty.data.repository

import com.dscvit.vitty.data.dao.NoteDao
import com.dscvit.vitty.data.entity.NoteEntity
import com.dscvit.vitty.ui.coursepage.Note
import com.dscvit.vitty.ui.coursepage.NoteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(
    private val noteDao: NoteDao
) {
    
    fun getNotesByCourse(courseId: String): Flow<List<Note>> {
        return noteDao.getNotesByCourse(courseId).map { entities ->
            entities.map { it.toNote() }
        }
    }
    
    fun getStarredNotesByCourse(courseId: String): Flow<List<Note>> {
        return noteDao.getStarredNotesByCourse(courseId).map { entities ->
            entities.map { it.toNote() }
        }
    }
    
    suspend fun getNoteById(noteId: Long): Note? {
        return noteDao.getNoteById(noteId)?.toNote()
    }
    
    suspend fun insertNote(note: Note, courseId: String): Long {
        return noteDao.insertNote(note.toEntity(courseId))
    }
    
    suspend fun updateNote(note: Note, courseId: String, noteId: Long) {
        noteDao.updateNote(note.toEntity(courseId, noteId))
    }
    
    suspend fun deleteNote(note: Note, courseId: String, noteId: Long) {
        noteDao.deleteNote(note.toEntity(courseId, noteId))
    }
    
    suspend fun deleteNotesByCourse(courseId: String) {
        noteDao.deleteNotesByCourse(courseId)
    }
    
    suspend fun updateStarredStatus(noteId: Long, isStarred: Boolean) {
        noteDao.updateStarredStatus(noteId, isStarred)
    }
    
    fun searchNotes(courseId: String, query: String): Flow<List<Note>> {
        return noteDao.searchNotes(courseId, query).map { entities ->
            entities.map { it.toNote() }
        }
    }
    
    private fun NoteEntity.toNote(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            type = type,
            isStarred = isStarred,
            imagePath = imagePath
        )
    }
    
    private fun Note.toEntity(courseId: String, id: Long = 0): NoteEntity {
        return NoteEntity(
            id = if (id == 0L) this.id else id,
            courseId = courseId,
            title = title,
            content = content,
            type = type,
            isStarred = isStarred,
            imagePath = imagePath,
            updatedAt = System.currentTimeMillis()
        )
    }
}
