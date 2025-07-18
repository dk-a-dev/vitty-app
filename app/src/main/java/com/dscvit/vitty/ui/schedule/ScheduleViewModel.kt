package com.dscvit.vitty.ui.schedule

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dscvit.vitty.network.api.community.APICommunityRestClient
import com.dscvit.vitty.network.api.community.RetrofitSelfUserListener
import com.dscvit.vitty.network.api.community.RetrofitTimetableListener
import com.dscvit.vitty.network.api.community.responses.timetable.TimetableResponse
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import retrofit2.Call
import timber.log.Timber

class ScheduleViewModel : ViewModel() {
    private val _user = MutableLiveData<UserResponse?>()
    private val _timetable = MutableLiveData<TimetableResponse?>()

    val user: MutableLiveData<UserResponse?> = _user
    val timetable: MutableLiveData<TimetableResponse?> = _timetable

    fun getUserWithTimeTable(
        token: String,
        username: String,
    ) {
        Timber.d("Fetching user with timetable: token: $token, username: $username")
        APICommunityRestClient.instance.getUserWithTimeTable(
            token,
            username,
            object : RetrofitSelfUserListener {
                override fun onSuccess(
                    call: Call<UserResponse>?,
                    response: UserResponse?,
                ) {
                    Timber.d("User with timetable fetched successfully: $response")
                    _user.postValue(response)
                }

                override fun onError(
                    call: Call<UserResponse>?,
                    t: Throwable?,
                ) {
                    Timber.e("Error fetching user with timetable: ${t?.message}")
                    _user.postValue(null)
                }
            },
        )
    }

    fun getTimeTable(
        token: String,
        username: String,
    ) {
        Timber.d("Fetching user with timetable: token: $token, username: $username")
        APICommunityRestClient.instance.getTimeTable(
            token,
            username,
            object : RetrofitTimetableListener {
                override fun onSuccess(
                    call: Call<TimetableResponse>?,
                    response: TimetableResponse?,
                ) {
                    Timber.d("User with timetable fetched successfully: $response")
                    _timetable.postValue(response)
                }

                override fun onError(
                    call: Call<TimetableResponse>?,
                    t: Throwable?,
                ) {
                    Timber.e("Error fetching user with timetable: ${t?.message}")
                    _timetable.postValue(null)
                }
            },
        )
    }

    fun getCircleTimeTable(
        token: String,
        circleId: String,
        username: String,
    ) {
        APICommunityRestClient.instance.getCircleTimeTable(
            token,
            circleId,
            username,
            object : RetrofitTimetableListener {
                override fun onSuccess(
                    call: Call<TimetableResponse>?,
                    response: TimetableResponse?,
                ) {
                    Timber.d("Circle timetable fetched successfully: $response")
                    _timetable.postValue(response)
                }

                override fun onError(
                    call: Call<TimetableResponse>?,
                    t: Throwable?,
                ) {
                    Timber.e("Error fetching circle timetable: ${t?.message}")
                    _timetable.postValue(null)
                }
            },
        )
    }
}
