package com.dscvit.vitty.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dscvit.vitty.theme.VittyTheme

class NoteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                VittyTheme {
                    NoteScreenContent(
                        onBackClick = {
                            findNavController().popBackStack()
                        },
                    )
                }
            }
        }
}
