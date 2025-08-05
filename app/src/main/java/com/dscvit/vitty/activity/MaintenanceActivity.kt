package com.dscvit.vitty.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityMaintenanceBinding
import com.dscvit.vitty.theme.VittyTheme
import com.dscvit.vitty.ui.maintenance.MaintenanceScreen
import com.dscvit.vitty.util.MaintenanceChecker

class MaintenanceActivity : FragmentActivity() {

    private lateinit var binding: ActivityMaintenanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maintenance)
        
        binding.composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@MaintenanceActivity)
            )
            setContent {
                VittyTheme {
                    MaintenanceScreen(
                        onRetryClick = { retryConnection() },
                        onExitClick = { exitApp() }
                    )
                }
            }
        }
    }

    private fun retryConnection() {
        MaintenanceChecker.checkMaintenanceStatusAsync(this) { isUnderMaintenance ->
            if (!isUnderMaintenance) {
                val intent = Intent(this, InstructionsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun exitApp() {
        finishAffinity()
    }
}
