package com.dscvit.vitty.util

import android.content.Context
import com.dscvit.vitty.util.WebConstants.COMMUNITY_BASE_URL
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.TimeUnit

object MaintenanceChecker {
    
    private const val MAINTENANCE_CHECK_TIMEOUT = 5L
    private const val API_OPERATIONAL_MESSAGE = "Welcome to VITTY API!🎉"
    
    private var isChecking = false
    
    fun checkMaintenanceStatusAsync(
        context: Context,
        onResult: (isUnderMaintenance: Boolean) -> Unit
    ) {
        if (isChecking) return
        
        isChecking = true
        
        CoroutineScope(Dispatchers.IO).launch {
            val isUnderMaintenance = try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(MAINTENANCE_CHECK_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(MAINTENANCE_CHECK_TIMEOUT, TimeUnit.SECONDS)
                    .callTimeout(MAINTENANCE_CHECK_TIMEOUT, TimeUnit.SECONDS)
                    .build()
                
                val request = Request.Builder()
                    .url(COMMUNITY_BASE_URL)
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                val result = when {
                    !response.isSuccessful -> {
                        Timber.d("Server returned error code: ${response.code}")
                        true // Server error = maintenance
                    }
                    responseBody == null -> {
                        Timber.d("No response body received")
                        true // No response = maintenance
                    }
                    responseBody.contains(API_OPERATIONAL_MESSAGE) -> {
                        Timber.d("API operational message found")
                        false // API working
                    }
                    else -> {
                        Timber.d("Different response received: $responseBody")
                        true // Different response = maintenance
                    }
                }
                
                Timber.d("Maintenance check: isUnderMaintenance=$result, response=$responseBody")
                result
                
            } catch (e: Exception) {
                Timber.e(e, "Maintenance check failed")
                false // Network error = don't assume maintenance, just fail silently
            }
            
            withContext(Dispatchers.Main) {
                isChecking = false
                onResult(isUnderMaintenance)
            }
        }
    }
    
    fun isNetworkAvailable(context: Context): Boolean {
        return UtilFunctions.isNetworkAvailable(context)
    }
}
