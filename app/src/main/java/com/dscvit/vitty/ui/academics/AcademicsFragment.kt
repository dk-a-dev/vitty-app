package com.dscvit.vitty.ui.academics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.dscvit.vitty.databinding.FragmentAcademicsBinding
import com.dscvit.vitty.theme.AcademicsTheme
import com.dscvit.vitty.util.Constants

class AcademicsFragment : Fragment() {
    private var fragmentAcademicsBinding: FragmentAcademicsBinding? = null
    private val binding get() = fragmentAcademicsBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragmentAcademicsBinding = FragmentAcademicsBinding.inflate(inflater, container, false)
        val view = binding.root

        val prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
        val profilePictureUrl = prefs.getString(Constants.COMMUNITY_PICTURE, null)

        binding.academicsComposeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner),
            )
            setContent {
                AcademicsTheme {
                    AcademicsScreenContent(
                        profilePictureUrl = profilePictureUrl,
                    )
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentAcademicsBinding = null
    }
}
