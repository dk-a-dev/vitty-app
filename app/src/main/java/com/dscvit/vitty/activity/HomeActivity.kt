
package com.dscvit.vitty.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityHomeBinding

class HomeActivity : FragmentActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val fadeIn =
            android.view.animation.AnimationUtils
                .loadAnimation(this, R.anim.fade_in)
        val fadeOut =
            android.view.animation.AnimationUtils
                .loadAnimation(this, R.anim.fade_out)

        fun navigateIfNeeded(destinationId: Int) {
            if (navController.currentDestination?.id != destinationId) {
                navController.navigate(destinationId)
            }
        }

        fun highlightSelectedTab(selectedId: Int) {
            
            with(binding) {
                navAcademics.setBackgroundResource(0)
                navTimetable.setBackgroundResource(0)
                navCommunity.setBackgroundResource(0)

                textAcademics.visibility = View.GONE
                textTimetable.visibility = View.GONE
                textCommunity.visibility = View.GONE
            }

            
            when (selectedId) {
                R.id.navigation_academics -> {
                    binding.navAcademics.setBackgroundResource(R.drawable.bg_nav_item_selected)
                    binding.textAcademics.visibility = View.VISIBLE
                }
                R.id.navigation_schedule -> {
                    binding.navTimetable.setBackgroundResource(R.drawable.bg_nav_item_selected)
                    binding.textTimetable.visibility = View.VISIBLE
                }
                R.id.navigation_community -> {
                    binding.navCommunity.setBackgroundResource(R.drawable.bg_nav_item_selected)
                    binding.textCommunity.visibility = View.VISIBLE
                }
            }
        }

        
        binding.navAcademics.setOnClickListener {
            navigateIfNeeded(R.id.navigation_academics)
        }

        binding.navTimetable.setOnClickListener {
            navigateIfNeeded(R.id.navigation_schedule)
        }

        binding.navCommunity.setOnClickListener {
            navigateIfNeeded(R.id.navigation_community)
        }

        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.allRequestFragment,
                R.id.friendFragment,
                R.id.searchFragment,
                R.id.navigation_requests,
                -> {
                    if (binding.customBottomNav.visibility == View.VISIBLE) {
                        binding.customBottomNav.startAnimation(fadeOut)
                        binding.customBottomNav.visibility = View.GONE
                    }
                }

                R.id.navigation_academics -> {
                    highlightSelectedTab(R.id.navigation_academics)
                    if (binding.customBottomNav.visibility != View.VISIBLE) {
                        binding.customBottomNav.startAnimation(fadeIn)
                        binding.customBottomNav.visibility = View.VISIBLE
                    }
                }

                R.id.navigation_schedule -> {
                    highlightSelectedTab(R.id.navigation_schedule)
                    if (binding.customBottomNav.visibility != View.VISIBLE) {
                        binding.customBottomNav.startAnimation(fadeIn)
                        binding.customBottomNav.visibility = View.VISIBLE
                    }
                }

                R.id.navigation_community -> {
                    highlightSelectedTab(R.id.navigation_community)
                    if (binding.customBottomNav.visibility != View.VISIBLE) {
                        binding.customBottomNav.startAnimation(fadeIn)
                        binding.customBottomNav.visibility = View.VISIBLE
                    }
                }

                else -> {
                    if (binding.customBottomNav.visibility != View.VISIBLE) {
                        binding.customBottomNav.startAnimation(fadeIn)
                        binding.customBottomNav.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}


// HomeActivity.kt with Material Bottom Navigation
// package com.dscvit.vitty.activity

// import android.os.Bundle
// import android.view.View
// import androidx.databinding.DataBindingUtil
// import androidx.fragment.app.FragmentActivity
// import androidx.navigation.findNavController
// import androidx.navigation.ui.setupWithNavController
// import com.dscvit.vitty.R
// import com.dscvit.vitty.databinding.ActivityHomeBinding
// import com.google.android.material.bottomnavigation.BottomNavigationView

// class HomeActivity : FragmentActivity() {

//     private lateinit var binding: ActivityHomeBinding

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

//         val navView: BottomNavigationView = binding.navView

//         val navController = findNavController(R.id.nav_host_fragment_activity_main)

//         navView.setupWithNavController(navController)


//         navController.addOnDestinationChangedListener { _, destination, _ ->
//             if (destination.id == R.id.allRequestFragment || destination.id == R.id.friendFragment || destination.id == R.id.searchFragment || destination.id == R.id.navigation_requests) {

//                 binding.navView.visibility = View.GONE
//             } else {

//                 binding.navView.visibility = View.VISIBLE
//             }
//         }
//     }


// }
