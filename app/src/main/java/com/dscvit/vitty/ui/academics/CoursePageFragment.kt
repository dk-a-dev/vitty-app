package com.dscvit.vitty.ui.academics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.FragmentCoursePageBinding
import com.dscvit.vitty.theme.VittyTheme

class CoursePageFragment : Fragment() {
    private var fragmentCoursePageBinding: FragmentCoursePageBinding? = null
    private val binding get() = fragmentCoursePageBinding!!
    private val args: CoursePageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragmentCoursePageBinding = FragmentCoursePageBinding.inflate(inflater, container, false)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                VittyTheme {                    CoursePageContent(
                        courseTitle = args.courseTitle,
                        courseSlot = args.courseSlot,
                        courseCode = args.courseCode,
                        onBackClick = { findNavController().popBackStack() },
                        onNavigateToNote = { 
                            findNavController().navigate(R.id.action_coursePageFragment_to_noteFragment)
                        },
                    )
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentCoursePageBinding = null
    }
}
