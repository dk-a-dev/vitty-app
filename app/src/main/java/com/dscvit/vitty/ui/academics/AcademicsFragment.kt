package com.dscvit.vitty.ui.academics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.dscvit.vitty.databinding.FragmentAcademicsBinding
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.VittyTheme
import com.dscvit.vitty.ui.academics.models.Course
import com.dscvit.vitty.ui.schedule.ScheduleViewModel
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.SemesterUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AcademicsFragment : Fragment() {
    private var fragmentAcademicsBinding: FragmentAcademicsBinding? = null
    private val binding get() = fragmentAcademicsBinding!!
    private lateinit var scheduleViewModel: ScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragmentAcademicsBinding = FragmentAcademicsBinding.inflate(inflater, container, false)
        val view = binding.root

        scheduleViewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]

        binding.composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner),
            )
            setContent {
                VittyTheme {
                    val prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
                    val profilePictureUrl = prefs.getString(Constants.COMMUNITY_PICTURE, null)

                    val userResponse by scheduleViewModel.user.observeAsState()

                    var allCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
                    var isLoading by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        val token = prefs.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                        val username = prefs.getString(Constants.COMMUNITY_USERNAME, null) ?: ""

                        allCourses = loadCachedCourses(prefs)

                        if (token.isNotEmpty() && username.isNotEmpty()) {
                            isLoading = true
                            scheduleViewModel.getUserWithTimeTable(token, username)
                        }
                    }

                    LaunchedEffect(userResponse) {
                        userResponse?.let { response ->
                            isLoading = false

                            allCourses =
                                withContext(Dispatchers.Default) {
                                    extractCoursesFromTimetable(response)
                                }
                        }
                    }

                    AcademicsScreenContent(
                        profilePictureUrl = profilePictureUrl,
                        allCourses = allCourses,
                        onCourseClick = { course ->
                            val action =
                                AcademicsFragmentDirections
                                    .actionNavigationAcademicsToCoursePageFragment(
                                        courseTitle = course.title,
                                        courseSlot = course.slot,
                                        courseCode = course.code,
                                        courseSemester = course.semester,
                                    )
                            findNavController().navigate(action)
                        },
                        academicsViewModel = viewModel(),
                        coursePageViewModel = viewModel(),
                    )
                }
            }
        }

        return view
    }

    private suspend fun loadCachedCourses(prefs: android.content.SharedPreferences): List<Course> =
        withContext(Dispatchers.IO) {
            val cachedData = prefs.getString(Constants.CACHE_COMMUNITY_TIMETABLE, null)
            if (cachedData != null) {
                try {
                    val userResponse = Gson().fromJson(cachedData, UserResponse::class.java)
                    extractCoursesFromTimetable(userResponse)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

    private fun extractCoursesFromTimetable(userResponse: UserResponse): List<Course> {
        val timetableData = userResponse.timetable?.data ?: return emptyList()

        val allLectures =
            listOfNotNull(
                timetableData.Monday,
                timetableData.Tuesday,
                timetableData.Wednesday,
                timetableData.Thursday,
                timetableData.Friday,
                timetableData.Saturday,
                timetableData.Sunday,
            ).flatten()

        val currentSemester = SemesterUtils.determineSemester()

        val groupedLectures = allLectures.groupBy { it.name }

        val result = mutableListOf<Course>()

        groupedLectures.keys.sorted().forEach { title ->
            val lectures = groupedLectures[title] ?: return@forEach

            val uniqueSlots =
                lectures
                    .map { it.slot }
                    .toSet()
                    .sorted()
                    .joinToString(" + ")
            val uniqueCodes =
                lectures
                    .map { it.code }
                    .toSet()
                    .sorted()
                    .joinToString(" / ")

            result.add(
                Course(
                    title = title,
                    slot = uniqueSlots,
                    code = uniqueCodes,
                    semester = currentSemester,
                    isStarred = false,
                ),
            )
        }

        return result.sortedBy { it.title }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAcademicsBinding = null
    }
}
