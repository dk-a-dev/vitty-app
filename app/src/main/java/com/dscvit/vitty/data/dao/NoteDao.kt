package com.dscvit.vitty.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dscvit.vitty.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getNotesByCourse(courseId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE courseId = :courseId AND isStarred = 1 ORDER BY createdAt DESC")
    fun getStarredNotesByCourse(courseId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?

    @Insert
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE courseId = :courseId")
    suspend fun deleteNotesByCourse(courseId: String)

    @Query("UPDATE notes SET isStarred = :isStarred WHERE id = :noteId")
    suspend fun updateStarredStatus(
        noteId: Long,
        isStarred: Boolean,
    )

    @Query(
        "SELECT * FROM notes WHERE courseId = :courseId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY createdAt DESC",
    )
    fun searchNotes(
        courseId: String,
        query: String,
    ): Flow<List<NoteEntity>>
}
