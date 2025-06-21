package com.dscvit.vitty.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityHomeBinding

class HomeActivity : FragmentActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var selectedBackground: View? = null
    private var currentSelectedId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        fun navigateIfNeeded(destinationId: Int) {
            if (navController.currentDestination?.id != destinationId) {
                navController.navigate(destinationId)
            }
        }

        fun findImageViewInLayout(layout: LinearLayout): ImageView? {
            for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child is ImageView) {
                    return child
                }
            }
            return null
        }

        fun animateIconBounce(layout: LinearLayout) {
            val imageView = findImageViewInLayout(layout) ?: return

            val scaleX =
                ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.4f, 1f).apply {
                    duration = 500
                    interpolator = OvershootInterpolator(2f)
                }
            val scaleY =
                ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.4f, 1f).apply {
                    duration = 500
                    interpolator = OvershootInterpolator(2f)
                }

            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                start()
            }
        }

        fun animateTextSlideIn(textView: TextView) {
            textView.alpha = 0f
            textView.translationX = 30f
            textView.visibility = View.VISIBLE

            val slideAnimator =
                ObjectAnimator.ofFloat(textView, "translationX", 30f, 0f).apply {
                    duration = 600
                    interpolator = OvershootInterpolator(1.2f)
                }
            val fadeAnimator =
                ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f).apply {
                    duration = 400
                }

            AnimatorSet().apply {
                playTogether(slideAnimator, fadeAnimator)
                start()
            }
        }

        fun animateTextSlideOut(
            textView: TextView,
            onComplete: () -> Unit = {},
        ) {
            if (textView.visibility != View.VISIBLE) {
                onComplete()
                return
            }

            val slideAnimator =
                ObjectAnimator.ofFloat(textView, "translationX", 0f, -30f).apply {
                    duration = 250
                }
            val fadeAnimator =
                ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f).apply {
                    duration = 250
                }

            AnimatorSet().apply {
                playTogether(slideAnimator, fadeAnimator)
                addListener(
                    object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            textView.visibility = View.GONE
                            textView.translationX = 0f 
                            onComplete()
                        }
                    },
                )
                start()
            }
        }

        fun animatePillMovement(
            fromView: View?,
            toView: View,
            onComplete: () -> Unit = {},
        ) {
            
            binding.navAcademics.setBackgroundResource(0)
            binding.navTimetable.setBackgroundResource(0)
            binding.navCommunity.setBackgroundResource(0)

            if (fromView == null) {
                
                toView.setBackgroundResource(R.drawable.bg_nav_item_selected)
                toView.alpha = 0f
                toView.scaleX = 0.9f
                toView.scaleY = 0.9f

                val fadeAnim =
                    ObjectAnimator.ofFloat(toView, "alpha", 0f, 1f).apply {
                        duration = 200
                    }
                val scaleXAnim =
                    ObjectAnimator.ofFloat(toView, "scaleX", 0.9f, 1f).apply {
                        duration = 300
                        interpolator = OvershootInterpolator(1.5f)
                    }
                val scaleYAnim =
                    ObjectAnimator.ofFloat(toView, "scaleY", 0.9f, 1f).apply {
                        duration = 300
                        interpolator = OvershootInterpolator(1.5f)
                    }

                AnimatorSet().apply {
                    playTogether(fadeAnim, scaleXAnim, scaleYAnim)
                    addListener(
                        object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                onComplete()
                            }
                        },
                    )
                    start()
                }
                return
            }

            
            toView.setBackgroundResource(R.drawable.bg_nav_item_selected)

            val morphAnimator =
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 400
                    interpolator = OvershootInterpolator(0.8f)

                    addUpdateListener { animator ->
                        val progress = animator.animatedValue as Float

                        
                        val scale = 1f + (0.1f * kotlin.math.sin(progress * kotlin.math.PI)).toFloat()
                        toView.scaleX = scale
                        toView.scaleY = scale
                    }

                    addListener(
                        object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                
                                val resetScaleX =
                                    ObjectAnimator.ofFloat(toView, "scaleX", toView.scaleX, 1f).apply {
                                        duration = 100
                                        interpolator = OvershootInterpolator(1f)
                                    }
                                val resetScaleY =
                                    ObjectAnimator.ofFloat(toView, "scaleY", toView.scaleY, 1f).apply {
                                        duration = 100
                                        interpolator = OvershootInterpolator(1f)
                                    }

                                AnimatorSet().apply {
                                    playTogether(resetScaleX, resetScaleY)
                                    addListener(
                                        object : android.animation.AnimatorListenerAdapter() {
                                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                                onComplete()
                                            }
                                        },
                                    )
                                    start()
                                }
                            }
                        },
                    )
                }
            morphAnimator.start()
        }

        fun highlightSelectedTab(selectedId: Int) {
            if (currentSelectedId == selectedId) return

            val (newSelectedView, newTextView) =
                when (selectedId) {
                    R.id.navigation_academics -> Pair(binding.navAcademics, binding.textAcademics)
                    R.id.navigation_schedule -> Pair(binding.navTimetable, binding.textTimetable)
                    R.id.navigation_community -> Pair(binding.navCommunity, binding.textCommunity)
                    else -> return
                }

            
            val allTextViews = listOf(binding.textAcademics, binding.textTimetable, binding.textCommunity)
            val visibleTextViews = allTextViews.filter { it.visibility == View.VISIBLE }

            var completedAnimations = 0
            val totalAnimations = visibleTextViews.size

            fun checkAllAnimationsComplete() {
                completedAnimations++
                if (completedAnimations >= totalAnimations || totalAnimations == 0) {
                    
                    animatePillMovement(selectedBackground, newSelectedView) {
                        
                        animateTextSlideIn(newTextView)
                        animateIconBounce(newSelectedView)
                    }
                }
            }

            if (visibleTextViews.isNotEmpty()) {
                visibleTextViews.forEach { textView ->
                    animateTextSlideOut(textView) {
                        checkAllAnimationsComplete()
                    }
                }
            } else {
                checkAllAnimationsComplete()
            }

            selectedBackground = newSelectedView
            currentSelectedId = selectedId
        }

        fun animateButtonPress(view: View) {
            val scaleDown =
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f).apply {
                    duration = 100
                }
            val scaleDownY =
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f).apply {
                    duration = 100
                }
            val scaleUp =
                ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f).apply {
                    duration = 150
                    interpolator = OvershootInterpolator(2f)
                }
            val scaleUpY =
                ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f).apply {
                    duration = 150
                    interpolator = OvershootInterpolator(2f)
                }

            AnimatorSet().apply {
                play(scaleDown).with(scaleDownY)
                play(scaleUp).after(scaleDown).with(scaleUpY)
                start()
            }
        }

        
        binding.navAcademics.setOnClickListener {
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) {
                
            }
            animateButtonPress(it)
            navigateIfNeeded(R.id.navigation_academics)
        }

        binding.navTimetable.setOnClickListener {
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) {
                
            }
            animateButtonPress(it)
            navigateIfNeeded(R.id.navigation_schedule)
        }

        binding.navCommunity.setOnClickListener {
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) {
                
            }
            animateButtonPress(it)
            navigateIfNeeded(R.id.navigation_community)
        }

        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.allRequestFragment,
                R.id.friendFragment,
                R.id.searchFragment,
                R.id.navigation_requests,
                -> {
                    hideBottomNavSafely()
                }

                R.id.navigation_academics -> {
                    showBottomNavSafely()
                    highlightSelectedTab(R.id.navigation_academics)
                }

                R.id.navigation_schedule -> {
                    showBottomNavSafely()
                    highlightSelectedTab(R.id.navigation_schedule)
                }

                R.id.navigation_community -> {
                    showBottomNavSafely()
                    highlightSelectedTab(R.id.navigation_community)
                }

                else -> {
                    showBottomNavSafely()
                }
            }
        }
    }

    private fun showBottomNavSafely() {
        if (binding.customBottomNav.visibility != View.VISIBLE) {
            binding.customBottomNav.visibility = View.VISIBLE
            binding.customBottomNav.translationY = 200f
            binding.customBottomNav.alpha = 0f

            val slideUp =
                ObjectAnimator.ofFloat(binding.customBottomNav, "translationY", 200f, 0f).apply {
                    duration = 400
                    interpolator = OvershootInterpolator(0.8f)
                }
            val fadeIn =
                ObjectAnimator.ofFloat(binding.customBottomNav, "alpha", 0f, 1f).apply {
                    duration = 300
                }

            AnimatorSet().apply {
                playTogether(slideUp, fadeIn)
                start()
            }
        }
    }

    private fun hideBottomNavSafely() {
        if (binding.customBottomNav.visibility == View.VISIBLE) {
            val slideDown =
                ObjectAnimator.ofFloat(binding.customBottomNav, "translationY", 0f, 200f).apply {
                    duration = 300
                }
            val fadeOut =
                ObjectAnimator.ofFloat(binding.customBottomNav, "alpha", 1f, 0f).apply {
                    duration = 250
                }

            AnimatorSet().apply {
                playTogether(slideDown, fadeOut)
                addListener(
                    object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            binding.customBottomNav.visibility = View.GONE
                            binding.customBottomNav.translationY = 0f 
                        }
                    },
                )
                start()
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
