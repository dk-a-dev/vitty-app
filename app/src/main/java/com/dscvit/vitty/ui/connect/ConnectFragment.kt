package com.dscvit.vitty.ui.connect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.dscvit.vitty.databinding.FragmentConnectBinding
import com.dscvit.vitty.theme.VittyTheme

class ConnectFragment : Fragment() {
    private var fragmentConnectBinding: FragmentConnectBinding? = null
    private val binding get() = fragmentConnectBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragmentConnectBinding = FragmentConnectBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner),
            )
            setContent {
                VittyTheme {
                    ConnectScreenContent(
                        onSearchClick = {},
                        onRequestsClick = {},
                    )
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentConnectBinding = null
    }
}
