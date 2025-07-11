package com.dscvit.vitty.activity

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityHomeComposeBinding
import com.dscvit.vitty.ui.main.MainComposeApp

class HomeComposeActivity : FragmentActivity() {
    private lateinit var binding: ActivityHomeComposeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_compose)

        binding.composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@HomeComposeActivity),
            )
            setContent {
                MainComposeApp()
            }
        }
    }
}
