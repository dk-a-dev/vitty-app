package com.dscvit.vitty.util

import android.content.Context
import com.dscvit.vitty.network.api.community.APICommunityRestClient
import com.dscvit.vitty.network.api.community.RetrofitServerStatusListener
import retrofit2.Call
import timber.log.Timber

object MaintenanceChecker {

    private const val API_OPERATIONAL_MESSAGE = "Welcome to VITTY API!🎉"
    private var isChecking = false

    fun checkMaintenanceStatusAsync(
        context: Context,
        onResult: (isUnderMaintenance: Boolean) -> Unit
    ) {
        if (isChecking) return

        isChecking = true

        APICommunityRestClient.instance.checkServerStatus(
            object : RetrofitServerStatusListener {
                override fun onSuccess(call: Call<String>, response: String?, isSuccessful: Boolean) {
                    val result = when {
                        !isSuccessful -> {
                            Timber.d("Server returned error")
                            true // Server error = maintenance
                        }
                        response == null -> {
                            Timber.d("No response body received")
                            true // No response = maintenance
                        }
                        response.contains(API_OPERATIONAL_MESSAGE) -> {
                            Timber.d("API operational message found")
                            false // API working
                        }
                        else -> {
                            Timber.d("Different response received: $response")
                            true // Different response = maintenance
                        }
                    }

                    Timber.d("Maintenance check: isUnderMaintenance=$result, response=$response")
                    isChecking = false
                    onResult(result)
                }

                override fun onError(call: Call<String>, t: Throwable) {
                    Timber.e(t, "Maintenance check failed")
                    isChecking = false
                    onResult(false) // Network error = don't assume maintenance
                }
            }
        )
    }
}