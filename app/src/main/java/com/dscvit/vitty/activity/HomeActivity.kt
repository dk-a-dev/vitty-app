package com.dscvit.vitty.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityHomeBinding

class HomeActivity : FragmentActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var selectedBackground: View? = null
    private var currentSelectedId = -1
    private var isAnimating = false
    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        fun createNavOptions(): NavOptions =
            NavOptions
                .Builder()
                .setEnterAnim(R.anim.crossfade_in)
                .setExitAnim(R.anim.crossfade_out)
                .setPopEnterAnim(R.anim.crossfade_in)
                .setPopExitAnim(R.anim.crossfade_out)
                .build()

        fun navigateIfNeeded(destinationId: Int) {
            if (navController.currentDestination?.id != destinationId && !isNavigating) {
                isNavigating = true

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    navController.navigate(destinationId, null, createNavOptions())

                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isNavigating = false
                    }, 250)
                }, 50)
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
                ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.15f, 1f).apply {
                    duration = 250
                    interpolator = OvershootInterpolator(1.0f)
                }
            val scaleY =
                ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.15f, 1f).apply {
                    duration = 250
                    interpolator = OvershootInterpolator(1.0f)
                }

            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                start()
            }
        }

        fun animateTextSlideIn(
            textView: TextView,
            delay: Long = 0,
        ) {
            textView.alpha = 0f
            textView.translationX = 10f
            textView.scaleX = 0.95f
            textView.visibility = View.VISIBLE

            val slideAnimator =
                ObjectAnimator.ofFloat(textView, "translationX", 10f, 0f).apply {
                    duration = 200
                    startDelay = delay
                    interpolator = AccelerateDecelerateInterpolator()
                }
            val fadeAnimator =
                ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f).apply {
                    duration = 150
                    startDelay = delay
                }
            val scaleAnimator =
                ObjectAnimator.ofFloat(textView, "scaleX", 0.95f, 1f).apply {
                    duration = 200
                    startDelay = delay
                    interpolator = OvershootInterpolator(0.6f)
                }

            AnimatorSet().apply {
                playTogether(slideAnimator, fadeAnimator, scaleAnimator)
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
                ObjectAnimator.ofFloat(textView, "translationX", 0f, -10f).apply {
                    duration = 100
                    interpolator = AccelerateDecelerateInterpolator()
                }
            val fadeAnimator =
                ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f).apply {
                    duration = 100
                }

            AnimatorSet().apply {
                playTogether(slideAnimator, fadeAnimator)
                addListener(
                    object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            textView.visibility = View.GONE
                            textView.translationX = 0f
                            textView.alpha = 1f
                            textView.scaleX = 1f
                            onComplete()
                        }

                        override fun onAnimationCancel(animation: android.animation.Animator) {
                            textView.visibility = View.GONE
                            textView.translationX = 0f
                            textView.alpha = 1f
                            textView.scaleX = 1f
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

            binding.navAcademics.elevation = 0f
            binding.navTimetable.elevation = 0f
            binding.navCommunity.elevation = 0f

            if (fromView == null) {
                toView.setBackgroundResource(R.drawable.bg_nav_item_selected)
                toView.alpha = 0f
                toView.scaleX = 0.95f
                toView.scaleY = 0.95f

                val fadeAnim =
                    ObjectAnimator.ofFloat(toView, "alpha", 0f, 1f).apply {
                        duration = 150
                    }
                val scaleXAnim =
                    ObjectAnimator.ofFloat(toView, "scaleX", 0.95f, 1f).apply {
                        duration = 180
                        interpolator = OvershootInterpolator(0.8f)
                    }
                val scaleYAnim =
                    ObjectAnimator.ofFloat(toView, "scaleY", 0.95f, 1f).apply {
                        duration = 180
                        interpolator = OvershootInterpolator(0.8f)
                    }
                val elevationAnim =
                    ObjectAnimator.ofFloat(toView, "elevation", 0f, 8f).apply {
                        duration = 180
                        interpolator = DecelerateInterpolator()
                    }

                AnimatorSet().apply {
                    playTogether(fadeAnim, scaleXAnim, scaleYAnim, elevationAnim)
                    addListener(
                        object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                onComplete()
                            }

                            override fun onAnimationCancel(animation: android.animation.Animator) {
                                onComplete()
                            }
                        },
                    )
                    start()
                }
                return
            }

            toView.setBackgroundResource(R.drawable.bg_nav_item_selected)

            val scaleX =
                ObjectAnimator.ofFloat(toView, "scaleX", 1f, 1.03f, 1f).apply {
                    duration = 150
                    interpolator = AccelerateDecelerateInterpolator()
                }
            val scaleY =
                ObjectAnimator.ofFloat(toView, "scaleY", 1f, 1.03f, 1f).apply {
                    duration = 150
                    interpolator = AccelerateDecelerateInterpolator()
                }
            val elevationAnim =
                ObjectAnimator.ofFloat(toView, "elevation", 0f, 8f).apply {
                    duration = 150
                    interpolator = DecelerateInterpolator()
                }

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, elevationAnim)
                addListener(
                    object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            onComplete()
                        }

                        override fun onAnimationCancel(animation: android.animation.Animator) {
                            onComplete()
                        }
                    },
                )
                start()
            }
        }

        fun highlightSelectedTab(selectedId: Int) {
            if (currentSelectedId == selectedId) return

            if (isAnimating) {
                binding.textAcademics.clearAnimation()
                binding.textTimetable.clearAnimation()
                binding.textCommunity.clearAnimation()
                binding.navAcademics.clearAnimation()
                binding.navTimetable.clearAnimation()
                binding.navCommunity.clearAnimation()
            }

            isAnimating = true

            val (newSelectedView, newTextView) =
                when (selectedId) {
                    R.id.navigation_academics -> Pair(binding.navAcademics, binding.textAcademics)
                    R.id.navigation_schedule -> Pair(binding.navTimetable, binding.textTimetable)
                    R.id.navigation_connect -> Pair(binding.navCommunity, binding.textCommunity)
                    else -> {
                        isAnimating = false
                        return
                    }
                }

            val allTextViews = listOf(binding.textAcademics, binding.textTimetable, binding.textCommunity)
            val visibleTextViews = allTextViews.filter { it.isVisible && it != newTextView }

            var completedOperations = 0
            val totalOperations = 1 + visibleTextViews.size

            fun onOperationComplete() {
                completedOperations++
                if (completedOperations >= totalOperations) {
                    isAnimating = false
                }
            }

            animatePillMovement(selectedBackground, newSelectedView) {
                onOperationComplete()
            }

            animateTextSlideIn(newTextView, delay = 25)

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                animateIconBounce(newSelectedView)
            }, 50)

            if (visibleTextViews.isEmpty()) {
                onOperationComplete()
            } else {
                visibleTextViews.forEach { textView ->
                    animateTextSlideOut(textView) {
                        onOperationComplete()
                    }
                }
            }

            selectedBackground = newSelectedView
            currentSelectedId = selectedId
        }

        fun animateButtonPress(view: View) {
            val scaleDown =
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f).apply {
                    duration = 60
                    interpolator = AccelerateDecelerateInterpolator()
                }
            val scaleDownY =
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f).apply {
                    duration = 60
                    interpolator = AccelerateDecelerateInterpolator()
                }
            val elevationDown =
                ObjectAnimator.ofFloat(view, "elevation", view.elevation, view.elevation * 0.5f).apply {
                    duration = 60
                }
            val scaleUp =
                ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f).apply {
                    duration = 100
                    interpolator = OvershootInterpolator(1.2f)
                }
            val scaleUpY =
                ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f).apply {
                    duration = 100
                    interpolator = OvershootInterpolator(1.2f)
                }
            val elevationUp =
                ObjectAnimator.ofFloat(view, "elevation", view.elevation * 0.5f, view.elevation).apply {
                    duration = 100
                    interpolator = OvershootInterpolator(0.6f)
                }

            AnimatorSet().apply {
                play(scaleDown).with(scaleDownY).with(elevationDown)
                play(scaleUp).after(scaleDown).with(scaleUpY).with(elevationUp)
                start()
            }
        }

        binding.navAcademics.setOnClickListener {
            if (isAnimating || isNavigating) return@setOnClickListener
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) {
            }
            animateButtonPress(it)
            navigateIfNeeded(R.id.navigation_academics)
        }

        binding.navTimetable.setOnClickListener {
            if (isAnimating || isNavigating) return@setOnClickListener
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) {
            }
            animateButtonPress(it)
            navigateIfNeeded(R.id.navigation_schedule)
        }

        binding.navCommunity.setOnClickListener {
            if (isAnimating || isNavigating) return@setOnClickListener
            try {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (e: Exception) {
            }
            animateButtonPress(it)
            navigateIfNeeded(R.id.navigation_connect)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                when (destination.id) {
                    R.id.allRequestFragment,
                    R.id.friendFragment,
                    R.id.searchFragment,
                    R.id.navigation_requests,
                    R.id.coursePageFragment,
                    R.id.noteFragment,
                    -> {
                        hideBottomNavSafely()
                    }

                    R.id.navigation_academics -> {
                        showBottomNavSafely()
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            highlightSelectedTab(R.id.navigation_academics)
                        }, 25)
                    }

                    R.id.navigation_schedule -> {
                        showBottomNavSafely()
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            highlightSelectedTab(R.id.navigation_schedule)
                        }, 25)
                    }

                    R.id.navigation_connect -> {
                        showBottomNavSafely()
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            highlightSelectedTab(R.id.navigation_connect)
                        }, 25)
                    }

                    else -> {
                        hideBottomNavSafely()
                    }
                }
            }, 50)
        }
    }

    private fun showBottomNavSafely() {
        if (binding.customBottomNav.visibility != View.VISIBLE) {
            binding.customBottomNav.visibility = View.VISIBLE
            binding.customBottomNav.translationY = 200f
            binding.customBottomNav.alpha = 0f
            binding.customBottomNav.scaleY = 0.9f

            val slideUp =
                ObjectAnimator.ofFloat(binding.customBottomNav, "translationY", 200f, 0f).apply {
                    duration = 300
                    interpolator = DecelerateInterpolator(1.0f)
                }
            val fadeIn =
                ObjectAnimator.ofFloat(binding.customBottomNav, "alpha", 0f, 1f).apply {
                    duration = 250
                    startDelay = 25
                }
            val scaleUp =
                ObjectAnimator.ofFloat(binding.customBottomNav, "scaleY", 0.9f, 1f).apply {
                    duration = 280
                    interpolator = OvershootInterpolator(0.4f)
                    startDelay = 50
                }

            AnimatorSet().apply {
                playTogether(slideUp, fadeIn, scaleUp)
                start()
            }
        }
    }

    private fun hideBottomNavSafely() {
        if (binding.customBottomNav.visibility == View.VISIBLE) {
            val slideDown =
                ObjectAnimator.ofFloat(binding.customBottomNav, "translationY", 0f, 200f).apply {
                    duration = 250
                    interpolator = AccelerateDecelerateInterpolator()
                }
            val fadeOut =
                ObjectAnimator.ofFloat(binding.customBottomNav, "alpha", 1f, 0f).apply {
                    duration = 200
                }
            val scaleDown =
                ObjectAnimator.ofFloat(binding.customBottomNav, "scaleY", 1f, 0.9f).apply {
                    duration = 220
                    interpolator = AccelerateDecelerateInterpolator()
                }

            AnimatorSet().apply {
                playTogether(slideDown, fadeOut, scaleDown)
                addListener(
                    object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            binding.customBottomNav.visibility = View.GONE
                            binding.customBottomNav.translationY = 0f
                            binding.customBottomNav.scaleY = 1f
                        }
                    },
                )
                start()
            }
        }
    }
}
