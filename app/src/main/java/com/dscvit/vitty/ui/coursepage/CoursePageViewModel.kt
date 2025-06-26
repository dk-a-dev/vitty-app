package com.dscvit.vitty.ui.coursepage

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dscvit.vitty.data.database.VittyDatabase
import com.dscvit.vitty.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CoursePageViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val noteRepository: NoteRepository

    init {
        val database = VittyDatabase.getDatabase(application)
        noteRepository = NoteRepository(database.noteDao())
    }

    private val _courseId = MutableStateFlow("")
    val courseId: StateFlow<String> = _courseId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> =
        combine(
            _courseId,
            _searchQuery,
        ) { courseId, query ->
            if (query.isBlank()) {
                noteRepository.getNotesByCourse(courseId)
            } else {
                noteRepository.searchNotes(courseId, query)
            }
        }.flatMapLatest { it }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    fun setCourseId(courseId: String) {
        _courseId.value = courseId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            noteRepository.insertNote(note, _courseId.value)
        }
    }

    fun updateNote(
        note: Note,
        noteId: Long,
    ) {
        viewModelScope.launch {
            noteRepository.updateNote(note, _courseId.value, noteId)
        }
    }

    fun deleteNote(
        note: Note,
        noteId: Long,
    ) {
        viewModelScope.launch {
            
            if (note.type == NoteType.IMAGE && !note.imagePath.isNullOrEmpty()) {
                deleteImageFile(note.imagePath)
            }
            noteRepository.deleteNote(note, _courseId.value, noteId)
        }
    }
    
    private suspend fun deleteImageFile(imagePath: String) = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleStarredStatus(note: Note) {
        viewModelScope.launch {
            noteRepository.updateStarredStatus(note.id.toLong(), !note.isStarred)
        }
    }

    
    fun addTextNote(
        title: String,
        content: String,
        isStarred: Boolean = false,
    ) {
        val note =
            Note(
                title = title,
                content = content,
                type = NoteType.TEXT,
                isStarred = isStarred,
            )
        addNote(note)
    }

    
    fun addImageNote(
        imageUri: String,
        isStarred: Boolean = false,
    ) {
        viewModelScope.launch {
            try {
                
                val localImagePath = copyImageToInternalStorage(Uri.parse(imageUri))
                localImagePath?.let { path ->
                    val note = Note(
                        title = "",
                        content = "",
                        type = NoteType.IMAGE,
                        isStarred = isStarred,
                        imagePath = path,
                    )
                    addNote(note)
                }
            } catch (e: Exception) {
                
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun copyImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>()
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            
            
            val timestamp = System.currentTimeMillis()
            val filename = "note_image_$timestamp.jpg"
            
            
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val outputFile = File(imagesDir, filename)
            val outputStream = FileOutputStream(outputFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            return@withContext outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    
    suspend fun getNoteById(noteId: String): Note? {
        return try {
            noteRepository.getNoteById(noteId.toLong())
        } catch (e: NumberFormatException) {
            null
        }
    }

    
    fun updateExistingNote(
        noteId: String,
        title: String,
        content: String,
        isStarred: Boolean = false,
    ) {
        viewModelScope.launch {
            try {
                val id = noteId.toLong()
                val note = Note(
                    id = id,
                    title = title,
                    content = content,
                    type = NoteType.TEXT,
                    isStarred = isStarred,
                )
                noteRepository.updateNote(note, _courseId.value, id)
            } catch (e: NumberFormatException) {
                
            }
        }
    }
}
