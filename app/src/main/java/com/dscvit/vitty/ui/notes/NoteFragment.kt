package com.dscvit.vitty.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dscvit.vitty.theme.VittyTheme
import com.dscvit.vitty.ui.coursepage.CoursePageViewModel
import com.dscvit.vitty.ui.coursepage.models.Note
import kotlinx.coroutines.launch

class NoteFragment : Fragment() {
    private val args: NoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                VittyTheme {
                    val viewModel: CoursePageViewModel = viewModel()
                    var noteToEdit by remember { mutableStateOf<Note?>(null) }
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(args.noteId) {
                        if (!args.noteId.isNullOrEmpty()) {
                            scope.launch {
                                noteToEdit = viewModel.getNoteById(args.noteId!!)
                            }
                        }
                    }

                    NoteScreenContent(
                        onBackClick = {
                            findNavController().popBackStack()
                        },
                        courseCode = args.courseCode,
                        noteToEdit = noteToEdit,
                        onSaveNote = { title, content ->
                            viewModel.setCourseId(args.courseCode)
                            if (noteToEdit != null) {
                                viewModel.updateExistingNote(
                                    noteId = noteToEdit!!.id.toString(),
                                    title = title,
                                    content = content,
                                    isStarred = noteToEdit!!.isStarred,
                                )
                            } else {
                                viewModel.addTextNote(title, content)
                            }
                            findNavController().popBackStack()
                        },
                    )
                }
            }
        }
}
