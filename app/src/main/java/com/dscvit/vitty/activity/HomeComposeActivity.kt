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
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

class HomeComposeActivity : FragmentActivity() {
    private lateinit var binding: ActivityHomeComposeBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var reviewManager: ReviewManager

    private var reviewInfo: ReviewInfo? = null

    companion object {
        private val REVIEW_INTERVAL_MILLIS = TimeUnit.DAYS.toMillis(30)
        private const val TAG = "HomeComposeActivity"
    }

    private val updateResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result: ActivityResult ->
            when (result.resultCode) {
                RESULT_OK -> {
                    Timber.d("$TAG:Update flow completed successfully")
                }
                RESULT_CANCELED -> {
                    Timber.d("$TAG:Update flow was cancelled by user")
                }
                else -> {
                    Timber.d("$TAG:Update flow failed with result code: ${result.resultCode}")
                }
            }
        }

    private val installStateUpdatedListener: InstallStateUpdatedListener =
        InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    showUpdateDownloadedSnackbar()
                }
                InstallStatus.INSTALLED -> {
                    appUpdateManager.unregisterListener(installStateUpdatedListener)
                }
                else -> {
                    Timber.d("$TAG:Install status: ${state.installStatus()}")
                }
            }
        }

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

        appUpdateManager.registerListener(installStateUpdatedListener)

        checkForAppUpdate()
        checkForReviewRequest()
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                Timber.d(
                    "$TAG:onResume - Update availability: ${appUpdateInfo.updateAvailability()}",
                )
                Timber.d("$TAG:onResume - Install status: ${appUpdateInfo.installStatus()}")

                if (appUpdateInfo.updateAvailability() ==
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    Timber.d("$TAG:Resuming in-progress update")
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                    )
                }

                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    showUpdateDownloadedSnackbar()
                }
            }.addOnFailureListener { exception ->
                Timber.e("$TAG:Failed to get app update info in onResume$exception")
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private fun checkForAppUpdate() {
        Timber.d("$TAG:Checking for app updates...")

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                Timber.d("$TAG:Update availability: ${appUpdateInfo.updateAvailability()}")
                Timber.d("$TAG:Update priority: ${appUpdateInfo.updatePriority()}")
                Timber.d(
                    "$TAG:Client version staleness days: ${appUpdateInfo.clientVersionStalenessDays()}",
                )
                Timber.d("$TAG:Available version code: ${appUpdateInfo.availableVersionCode()}")
                Timber.d("$TAG:Install status: ${appUpdateInfo.installStatus()}")

                when {
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        appUpdateInfo.updatePriority() >= 4 &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        Timber.d("$TAG:Starting immediate update (high priority)")
                        startImmediateUpdate(appUpdateInfo)
                    }
                    appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                        val stalenessDays = appUpdateInfo.clientVersionStalenessDays() ?: 0
                        Timber.d("$TAG:Update staleness: $stalenessDays days")

                        if (stalenessDays >= 1) {
                            Timber.d("$TAG:Starting flexible update (staleness criteria met)")
                            startFlexibleUpdate(appUpdateInfo)
                        } else {
                            Timber.d("$TAG:Update available but staleness criteria not met")
                        }
                    }
                    appUpdateInfo.updateAvailability() ==
                        UpdateAvailability.UPDATE_AVAILABLE -> {
                        Timber.d("$TAG:Update available but no update type allowed")
                        Timber.d(
                            "$TAG:Immediate allowed: ${appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)}",
                        )
                        Timber.d(
                            "$TAG:Flexible allowed: ${appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)}",
                        )
                    }
                    else -> {
                        Timber.d("$TAG:No update available or update not applicable")
                    }
                }
            }.addOnFailureListener { exception ->
                Timber.e("$TAG:Failed to check for app updates:$exception")
            }
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        Timber.d("Launching immediate update flow")
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            updateResultLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
        )
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        Timber.d("Launching flexible update flow")
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            updateResultLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
    }

    private fun showUpdateDownloadedSnackbar() {
        Timber.d("Showing update downloaded snackbar")
        Snackbar
            .make(
                binding.root,
                "An update has been downloaded.",
                Snackbar.LENGTH_INDEFINITE,
            ).apply {
                setAction("RESTART") {
                    Timber.d("User clicked restart - completing update")
                    appUpdateManager.completeUpdate()
                }
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
            } else {
                Timber.e("$TAG:Failed to request review info:${task.exception}")
            }
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
