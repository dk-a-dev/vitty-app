package com.dscvit.vitty.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.dscvit.vitty.R
import com.dscvit.vitty.adapter.IntroAdapter
import com.dscvit.vitty.databinding.ActivityAuthBinding
import com.dscvit.vitty.ui.auth.AuthViewModel
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.Constants.TOKEN
import com.dscvit.vitty.util.Constants.UID
import com.dscvit.vitty.util.Constants.USER_INFO
import com.dscvit.vitty.util.NotificationPermissionHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import timber.log.Timber

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    private val SIGNIN: Int = 1
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private lateinit var authViewModel: AuthViewModel

    private val pages = listOf("○", "○", "○")
    private var loginClick = false

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        firebaseAuth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences(USER_INFO, Context.MODE_PRIVATE)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupNotificationPermissionLauncher()
        requestNotificationPermissionIfNeeded()

        configureGoogleSignIn()
        setupUI()
    }

    private fun setupNotificationPermissionLauncher() {
        notificationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                if (isGranted) {
                    Timber.d("Notification permission granted")
                    // Check and request exact alarm permission if needed
                    if (!NotificationPermissionHelper.canScheduleExactAlarms(this)) {
                        NotificationPermissionHelper.requestExactAlarmPermission(this)
                    }
                } else {
                    Timber.d("Notification permission denied")
                    Toast
                        .makeText(
                            this,
                            "Notification permission is required for reminders to work properly",
                            Toast.LENGTH_LONG,
                        ).show()
                }
            }
    }

    private fun requestNotificationPermissionIfNeeded() {
        // For Android 13+ (API 33+), request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationPermissionHelper.hasNotificationPermission(this)) {
                Timber.d("Requesting POST_NOTIFICATIONS permission for Android 13+")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Timber.d("POST_NOTIFICATIONS permission already granted")
                checkAdditionalPermissions()
            }
        } else {
            // For Android 12 and below, notifications are enabled by default
            // but we still need to check other permissions
            Timber.d("Android version < 13, notifications enabled by default")
            checkAdditionalPermissions()
        }
    }

    private fun checkAdditionalPermissions() {
        // Check and request exact alarm permission if needed (Android 12+)
        if (!NotificationPermissionHelper.canScheduleExactAlarms(this)) {
            Timber.d("Requesting exact alarm permission")
            NotificationPermissionHelper.requestExactAlarmPermission(this)
        }

        // Note: Battery optimization can be checked later in the app flow
        // as it's more intrusive and not immediately necessary
    }

    override fun onStart() {
        super.onStart()
        sharedPref = getSharedPreferences(USER_INFO, Context.MODE_PRIVATE)
        val isTimeTableAvailable =
            sharedPref.getBoolean(Constants.COMMUNITY_TIMETABLE_AVAILABLE, false)
        val token = sharedPref.getString(Constants.COMMUNITY_TOKEN, null)
        val username = sharedPref.getString(Constants.COMMUNITY_USERNAME, null)
        val regno = sharedPref.getString(Constants.COMMUNITY_REGNO, null)
        val user = FirebaseAuth.getInstance().currentUser
        Timber.d("isTimeTableAvailable: $isTimeTableAvailable token: $token username: $username regno: $regno")
        if (isTimeTableAvailable) {
            val intent = Intent(this, HomeComposeActivity::class.java)
            startActivity(intent)
            finish()
        } else if (token != null && username != null) {
            val intent = Intent(this, InstructionsActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Timber.d("here going to add info")
            if (user != null) {
                val intent = Intent(this, AddInfoActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions =
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
        mGoogleSignInClient.signOut()
    }

    private fun setupUI() {
        val pagerAdapter = IntroAdapter(this)
        binding.introPager.adapter = pagerAdapter

        val pageChangeCallback =
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding.apply {
                        if (position == 0 || position == 1) {
                            loginButton.visibility = View.INVISIBLE
                            nextButton.visibility = View.VISIBLE
                        } else {
                            nextButton.visibility = View.INVISIBLE
                            loginButton.visibility = View.VISIBLE
                        }
                    }
                }
            }

        binding.introPager.registerOnPageChangeCallback(pageChangeCallback)

        TabLayoutMediator(
            binding.introTabs,
            binding.introPager,
        ) { tab, position -> tab.text = pages[position] }.attach()

        binding.nextButton.setOnClickListener {
            binding.apply {
                if (introPager.currentItem != pages.size - 1) {
                    introPager.currentItem++
                }
            }
        }

        binding.loginButton.setOnClickListener {
            login()
            if (loginClick) {
                binding.introPager.isUserInputEnabled = false
                binding.introPager.unregisterOnPageChangeCallback(pageChangeCallback)
            }
        }
    }

    private fun login() {
        binding.loadingView.visibility = View.VISIBLE
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, SIGNIN)
    }

    private fun logoutFailed() {
        Timber.e("Google sign-in failed - showing error message to user")
        Toast.makeText(this, getString(R.string.sign_in_fail), Toast.LENGTH_LONG).show()
        binding.loadingView.visibility = View.GONE
        binding.introPager.currentItem = 0
        loginClick = false
    }

    private fun saveInfo(
        token: String?,
        uid: String?,
    ) {
        with(sharedPref.edit()) {
            putString("simethod", "Google")
            putString(TOKEN, token)
            putString(UID, uid)
            apply()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("Activity Result - requestCode: $requestCode, resultCode: $resultCode")
        if (requestCode == SIGNIN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Timber.d("Google sign-in account: $account")
                if (account != null) {
                    Timber.d("Google sign-in successful, proceeding with Firebase auth")
                    firebaseAuthWithGoogle(account)
                } else {
                    Timber.e("Google sign-in account is null")
                    logoutFailed()
                }
            } catch (e: ApiException) {
                Timber.e("Google sign-in failed with ApiException: ${e.message}, statusCode: ${e.statusCode}")
                logoutFailed()
            } catch (e: Exception) {
                Timber.e("Google sign-in failed with Exception: ${e.message}")
                logoutFailed()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Timber.d("Starting Firebase authentication with Google account: ${acct.email}")
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        firebaseAuth
            .signInWithCredential(credential)
            .addOnCompleteListener { authResult ->
                if (authResult.isSuccessful) {
                    loginClick = true
                    val uid = firebaseAuth.currentUser?.uid
                    val email = firebaseAuth.currentUser?.email
                    Timber.d("Firebase authentication successful - uid: $uid, email: $email")
                    saveInfo(acct.idToken, uid)
                    authViewModel.signInAndGetTimeTable("", "", uid ?: "")
                    leadToNextPage()
                } else {
                    Timber.e("Firebase authentication failed: ${authResult.exception?.message}")
                    logoutFailed()
                }
            }.addOnFailureListener { exception ->
                Timber.e("Firebase authentication failed with exception: ${exception.message}")
                logoutFailed()
            }
    }

    private fun leadToNextPage() {
        authViewModel.signInResponse.observe(this) {
            if (it != null) {
                Timber.d("here--$it")
                sharedPref.edit().putString(Constants.COMMUNITY_USERNAME, it.username).apply()
                sharedPref.edit().putString(Constants.COMMUNITY_TOKEN, it.token).apply()
                sharedPref.edit().putString(Constants.COMMUNITY_NAME, it.name).apply()
                sharedPref.edit().putString(Constants.COMMUNITY_PICTURE, it.picture).apply()
            } else {
                val intent = Intent(this, AddInfoActivity::class.java)
                binding.loadingView.visibility = View.GONE
                startActivity(intent)
                finish()
            }
        }

        authViewModel.user.observe(this) {
            if (it != null) {
                val timetableDays = it.timetable?.data
                if (!timetableDays?.Monday.isNullOrEmpty() ||
                    !timetableDays?.Tuesday.isNullOrEmpty() ||
                    !timetableDays?.Wednesday.isNullOrEmpty() ||
                    !timetableDays?.Thursday.isNullOrEmpty() ||
                    !timetableDays?.Friday.isNullOrEmpty() ||
                    !timetableDays?.Saturday.isNullOrEmpty() ||
                    !timetableDays?.Sunday.isNullOrEmpty()
                ) {
                    sharedPref
                        .edit()
                        .putBoolean(Constants.COMMUNITY_TIMETABLE_AVAILABLE, true)
                        .apply()
                    val intent = Intent(this, HomeComposeActivity::class.java)
                    startActivity(intent)
                    finish()
                    binding.loadingView.visibility = View.GONE
                } else {
                    val intent = Intent(this, InstructionsActivity::class.java)
                    startActivity(intent)
                    finish()
                    binding.loadingView.visibility = View.GONE
                }
            }
        }
    }

    override fun onBackPressed() {
        binding.apply {
            if (introPager.currentItem == 0 || loginClick) {
                super.onBackPressed()
            } else {
                introPager.currentItem--
            }
        }
    }
}
