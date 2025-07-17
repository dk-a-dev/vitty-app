package com.dscvit.vitty.ui.connect

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dscvit.vitty.network.api.community.APICommunityRestClient
import com.dscvit.vitty.network.api.community.RetrofitCircleListener
import com.dscvit.vitty.network.api.community.RetrofitFriendListListener
import com.dscvit.vitty.network.api.community.RetrofitFriendRequestListener
import com.dscvit.vitty.network.api.community.RetrofitUserActionListener
import com.dscvit.vitty.network.api.community.responses.requests.RequestsResponse
import com.dscvit.vitty.network.api.community.responses.user.CircleResponse
import com.dscvit.vitty.network.api.community.responses.user.FriendResponse
import com.dscvit.vitty.network.api.community.responses.user.PostResponse
import retrofit2.Call
import timber.log.Timber

class ConnectViewModel : ViewModel() {
    private val _friendList = MutableLiveData<FriendResponse?>()
    private val _friendRequest = MutableLiveData<RequestsResponse?>()
    private val _requestActionResponse = MutableLiveData<PostResponse?>()
    private val _sendRequestResponse = MutableLiveData<PostResponse?>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _isRefreshing = MutableLiveData<Boolean>()
    private val _unfriendSuccess = MutableLiveData<String?>()
    private val _circleList = MutableLiveData<CircleResponse?>()
    private val _isCircleLoading = MutableLiveData<Boolean>()
    private val _isCircleRefreshing = MutableLiveData<Boolean>()
    private val _circleMembers = MutableLiveData<Map<String, FriendResponse>>()

    val friendList: MutableLiveData<FriendResponse?> = _friendList
    val friendRequest: MutableLiveData<RequestsResponse?> = _friendRequest
    val requestActionResponse: MutableLiveData<PostResponse?> = _requestActionResponse
    val sendRequestResponse: MutableLiveData<PostResponse?> = _sendRequestResponse
    val isLoading: MutableLiveData<Boolean> = _isLoading
    val isRefreshing: MutableLiveData<Boolean> = _isRefreshing
    val unfriendSuccess: MutableLiveData<String?> = _unfriendSuccess
    val circleList: MutableLiveData<CircleResponse?> = _circleList
    val isCircleLoading: MutableLiveData<Boolean> = _isCircleLoading
    val isCircleRefreshing: MutableLiveData<Boolean> = _isCircleRefreshing
    val circleMembers: MutableLiveData<Map<String, FriendResponse>> = _circleMembers

    fun getFriendList(
        token: String,
        username: String,
    ) {
        _isLoading.postValue(true)
        APICommunityRestClient.instance.getFriendList(
            token,
            username,
            object : RetrofitFriendListListener {
                override fun onSuccess(
                    call: Call<FriendResponse>?,
                    response: FriendResponse?,
                ) {
                    Timber.d("ConnectFriendList: $response")
                    _friendList.postValue(response)
                    _isLoading.postValue(false)
                }

                override fun onError(
                    call: Call<FriendResponse>?,
                    t: Throwable?,
                ) {
                    _friendList.postValue(null)
                    _isLoading.postValue(false)
                }
            },
        )
    }

    fun refreshFriendList(
        token: String,
        username: String,
    ) {
        _isRefreshing.postValue(true)
        APICommunityRestClient.instance.getFriendList(
            token,
            username,
            object : RetrofitFriendListListener {
                override fun onSuccess(
                    call: Call<FriendResponse>?,
                    response: FriendResponse?,
                ) {
                    _friendList.postValue(response)
                    _isRefreshing.postValue(false)
                }

                override fun onError(
                    call: Call<FriendResponse>?,
                    t: Throwable?,
                ) {
                    _friendList.postValue(null)
                    _isRefreshing.postValue(false)
                }
            },
        )
    }

    fun getFriendRequest(token: String) {
        APICommunityRestClient.instance.getFriendRequest(
            token,
            object : RetrofitFriendRequestListener {
                override fun onSuccess(
                    call: Call<RequestsResponse>?,
                    response: RequestsResponse?,
                ) {
                    Timber.d("ConnectFriendRequest: $response")
                    _friendRequest.postValue(response)
                }

                override fun onError(
                    call: Call<RequestsResponse>?,
                    t: Throwable?,
                ) {
                    _friendRequest.postValue(null)
                }
            },
        )
    }

    fun sendRequest(
        token: String,
        username: String,
    ) {
        APICommunityRestClient.instance.sendRequest(
            token,
            username,
            object : RetrofitUserActionListener {
                override fun onSuccess(
                    call: Call<PostResponse>?,
                    response: PostResponse?,
                ) {
                    Timber.d("ConnectSendRequest: $response")
                    _sendRequestResponse.postValue(response)
                }

                override fun onError(
                    call: Call<PostResponse>?,
                    t: Throwable?,
                ) {
                    Timber.d("ConnectSendRequestError: ${t?.message}")
                    _sendRequestResponse.postValue(null)
                }
            },
        )
    }

    fun unfriend(
        token: String,
        username: String,
    ) {
        APICommunityRestClient.instance.unfriend(
            token,
            username,
            object : RetrofitUserActionListener {
                override fun onSuccess(
                    call: Call<PostResponse>?,
                    response: PostResponse?,
                ) {
                    Timber.d("ConnectUnfriend: $response")
                    _unfriendSuccess.postValue(username)
                }

                override fun onError(
                    call: Call<PostResponse>?,
                    t: Throwable?,
                ) {
                    Timber.d("ConnectUnfriendError: ${t?.message}")
                    _unfriendSuccess.postValue(null)
                }
            },
        )
    }

    fun clearUnfriendSuccess() {
        _unfriendSuccess.postValue(null)
    }

    fun clearSendRequestResponse() {
        _sendRequestResponse.postValue(null)
    }

    fun clearRequestActionResponse() {
        _requestActionResponse.postValue(null)
    }

    fun acceptRequest(
        token: String,
        username: String,
    ) {
        APICommunityRestClient.instance.acceptRequest(
            token,
            username,
            object : RetrofitUserActionListener {
                override fun onSuccess(
                    call: Call<PostResponse>?,
                    response: PostResponse?,
                ) {
                    Timber.d("AcceptRequest: $response")
                    _requestActionResponse.postValue(response)
                }

                override fun onError(
                    call: Call<PostResponse>?,
                    t: Throwable?,
                ) {
                    Timber.d("AcceptRequest: ${t?.message}")
                }
            },
        )
    }

    fun rejectRequest(
        token: String,
        username: String,
    ) {
        APICommunityRestClient.instance.rejectRequest(
            token,
            username,
            object : RetrofitUserActionListener {
                override fun onSuccess(
                    call: Call<PostResponse>?,
                    response: PostResponse?,
                ) {
                    Timber.d("RejectRequest: $response")
                    _requestActionResponse.postValue(response)
                }

                override fun onError(
                    call: Call<PostResponse>?,
                    t: Throwable?,
                ) {
                    Timber.d("RejectRequest: ${t?.message}")
                }
            },
        )
    }

    fun getCircleList(token: String) {
        _isCircleLoading.postValue(true)
        APICommunityRestClient.instance.getCircles(
            token,
            object : RetrofitCircleListener {
                override fun onSuccess(
                    call: Call<CircleResponse>?,
                    response: CircleResponse?,
                ) {
                    Timber.d("ConnectCircleList: $response")
                    _circleList.postValue(response)
                    _isCircleLoading.postValue(false)
                }

                override fun onError(
                    call: Call<CircleResponse>?,
                    t: Throwable?,
                ) {
                    _circleList.postValue(null)
                    _isCircleLoading.postValue(false)
                }
            },
        )
    }

    fun refreshCircleList(token: String) {
        _isCircleRefreshing.postValue(true)
        APICommunityRestClient.instance.getCircles(
            token,
            object : RetrofitCircleListener {
                override fun onSuccess(
                    call: Call<CircleResponse>?,
                    response: CircleResponse?,
                ) {
                    Timber.d("ConnectCircleList: $response")
                    _circleList.postValue(response)
                    _isCircleRefreshing.postValue(false)
                }

                override fun onError(
                    call: Call<CircleResponse>?,
                    t: Throwable?,
                ) {
                    _circleList.postValue(null)
                    _isCircleRefreshing.postValue(false)
                }
            },
        )
    }

    fun getCircleDetails(
        token: String,
        circleId: String,
    ) {
        APICommunityRestClient.instance.getCircleDetails(
            token,
            circleId,
            object : RetrofitFriendListListener {
                override fun onSuccess(
                    call: Call<FriendResponse>?,
                    response: FriendResponse?,
                ) {
                    Timber.d("CircleDetails for $circleId: $response")
                    val currentMembers = _circleMembers.value?.toMutableMap() ?: mutableMapOf()
                    if (response != null) {
                        currentMembers[circleId] = response
                        _circleMembers.postValue(currentMembers)
                    }
                }

                override fun onError(
                    call: Call<FriendResponse>?,
                    t: Throwable?,
                ) {
                    Timber.d("CircleDetailsError for $circleId: ${t?.message}")
                }
            },
        )
    }

    fun fetchAllCircleDetails(token: String) {
        val circles = _circleList.value?.data ?: return
        circles.forEach { circle ->
            getCircleDetails(token, circle.circle_id)
        }
    }
}
