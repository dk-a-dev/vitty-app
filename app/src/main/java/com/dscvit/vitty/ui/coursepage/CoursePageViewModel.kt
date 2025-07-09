package com.dscvit.vitty.ui.coursepage

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dscvit.vitty.data.database.VittyDatabase
import com.dscvit.vitty.data.repository.NoteRepository
import com.dscvit.vitty.data.repository.ReminderRepository
import com.dscvit.vitty.receiver.ReminderNotificationManager
import com.dscvit.vitty.ui.coursepage.models.Note
import com.dscvit.vitty.ui.coursepage.models.NoteType
import com.dscvit.vitty.ui.coursepage.models.Reminder
import com.dscvit.vitty.ui.coursepage.models.ReminderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CoursePageViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val noteRepository: NoteRepository
    private val reminderRepository: ReminderRepository
    private val reminderNotificationManager: ReminderNotificationManager

    init {
        val database = VittyDatabase.getDatabase(application)
        noteRepository = NoteRepository(database.noteDao())
        reminderRepository = ReminderRepository(database.reminderDao())
        reminderNotificationManager = ReminderNotificationManager(application)
    }

    private val _courseId = MutableStateFlow("")
    val courseId: StateFlow<String> = _courseId.asStateFlow()

    private val _courseTitle = MutableStateFlow("")
    val courseTitle: StateFlow<String> = _courseTitle.asStateFlow()

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val reminders: StateFlow<List<Reminder>> =
        _courseId
            .flatMapLatest { courseId ->
                reminderRepository.getRemindersByCourse(courseId)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    fun setCourseId(courseId: String) {
        _courseId.value = courseId
    }

    fun setCourseTitle(courseTitle: String) {
        _courseTitle.value = courseTitle
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun addNote(note: Note) {
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

    private suspend fun deleteImageFile(imagePath: String) =
        withContext(Dispatchers.IO) {
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
            noteRepository.updateStarredStatus(note.id, !note.isStarred)
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
                val localImagePath = copyImageToInternalStorage(imageUri.toUri())
                localImagePath?.let { path ->
                    val note =
                        Note(
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

    private suspend fun copyImageToInternalStorage(uri: Uri): String? =
        withContext(Dispatchers.IO) {
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

    suspend fun getNoteById(noteId: String): Note? =
        try {
            noteRepository.getNoteById(noteId.toLong())
        } catch (e: NumberFormatException) {
            null
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
                val note =
                    Note(
                        id = id,
                        title = title,
                        content = content,
                        type = NoteType.TEXT,
                        isStarred = isStarred,
                    )
                noteRepository.updateNote(note, _courseId.value, id)
            } catch (e: NumberFormatException) {
                Timber.e(e, "Invalid note ID format")
            }
        }
    }

    fun addReminder(
        title: String,
        description: String,
        dateMillis: Long,
        fromTime: String,
        toTime: String,
        isAllDay: Boolean,
        alertDaysBefore: Int,
        attachmentUrl: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            try {
                val reminder =
                    Reminder(
                        title = title,
                        description = description,
                        dueDate = "",
                        status = ReminderStatus.UPCOMING,
                        dateMillis = dateMillis,
                        fromTime = fromTime,
                        toTime = toTime,
                        isAllDay = isAllDay,
                        alertDaysBefore = alertDaysBefore,
                        attachmentUrl = attachmentUrl.ifBlank { null },
                    )

                val reminderId = reminderRepository.insertReminder(reminder, _courseId.value, _courseTitle.value)

                reminderNotificationManager.scheduleReminderNotification(
                    reminderId = reminderId,
                    title = title,
                    description = description,
                    triggerTimeMillis = calculateNotificationTime(dateMillis, alertDaysBefore, fromTime, isAllDay),
                    courseTitle = _courseTitle.value,
                )

                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Error adding reminder")
                onError("Failed to create reminder: ${e.message}")
            }
        }
    }

    private fun calculateNotificationTime(
        dateMillis: Long,
        alertDaysBefore: Int,
        fromTime: String,
        isAllDay: Boolean,
    ): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = dateMillis

        if (!isAllDay && fromTime.isNotBlank()) {
            val timeParts = fromTime.split(":")
            if (timeParts.size >= 2) {
                val hour = timeParts[0].toIntOrNull() ?: 9
                val minute = timeParts[1].toIntOrNull() ?: 0
                calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                calendar.set(java.util.Calendar.MINUTE, minute)
            }
        } else {
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 9)
            calendar.set(java.util.Calendar.MINUTE, 0)
        }

        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        if (alertDaysBefore > 0) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -alertDaysBefore)
        }

        return calendar.timeInMillis
    }
}
