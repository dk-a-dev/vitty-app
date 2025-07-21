package com.dscvit.vitty.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityHomeComposeBinding
import com.dscvit.vitty.ui.main.MainComposeApp
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.Constants.PREF_LAST_REVIEW_REQUEST
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.concurrent.TimeUnit

class HomeComposeActivity : FragmentActivity() {
    private lateinit var binding: ActivityHomeComposeBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var reviewManager: ReviewManager

    private var reviewInfo: ReviewInfo? = null

    companion object {
        private val REVIEW_INTERVAL_MILLIS = TimeUnit.DAYS.toMillis(30)
    }

    private val updateResultLauncher =
            registerForActivityResult(
                    ActivityResultContracts.StartIntentSenderForResult(),
            ) { result: ActivityResult -> if (result.resultCode != RESULT_OK) {} }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_compose)
        prefs = getSharedPreferences(Constants.USER_INFO, 0)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        reviewManager = ReviewManagerFactory.create(this)

        binding.composeView.apply {
            setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@HomeComposeActivity),
            )
            setContent { MainComposeApp() }
        }

        checkForAppUpdate()
        checkForReviewRequest()
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() ==
                            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {

                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                )
            }

            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateDownloadedSnackbar()
            }
        }
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        appUpdateInfo.updatePriority() >= 4 &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    startImmediateUpdate(appUpdateInfo)
                }
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= 3 &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    startFlexibleUpdate(appUpdateInfo)
                }
            }
        }
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
        )
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
    }

    private fun showUpdateDownloadedSnackbar() {
        Snackbar.make(
                        binding.root,
                        "An update has been downloaded.",
                        Snackbar.LENGTH_INDEFINITE,
                )
                .apply {
                    setAction("RESTART") { appUpdateManager.completeUpdate() }
                    show()
                }
    }

    private fun checkForReviewRequest() {
        val lastReviewRequest = prefs.getLong(PREF_LAST_REVIEW_REQUEST, 0)
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastReviewRequest >= REVIEW_INTERVAL_MILLIS) {
            requestReviewInfo()
        }
    }

    private fun requestReviewInfo() {
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result

                launchReviewFlow()
            } else {}
        }
    }

    private fun launchReviewFlow() {
        reviewInfo?.let { info ->
            val flow = reviewManager.launchReviewFlow(this, info)
            flow.addOnCompleteListener {
                prefs.edit { putLong(PREF_LAST_REVIEW_REQUEST, System.currentTimeMillis()) }
                reviewInfo = null
            }
        }
    }
}
