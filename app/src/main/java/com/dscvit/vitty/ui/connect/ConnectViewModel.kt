package com.dscvit.vitty.ui.connect

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dscvit.vitty.network.api.community.APICommunityRestClient
import com.dscvit.vitty.network.api.community.RetrofitActiveFriendsListener
import com.dscvit.vitty.network.api.community.RetrofitCircleListener
import com.dscvit.vitty.network.api.community.RetrofitCircleRequestListener
import com.dscvit.vitty.network.api.community.RetrofitCreateCircleListener
import com.dscvit.vitty.network.api.community.RetrofitFriendListListener
import com.dscvit.vitty.network.api.community.RetrofitFriendRequestListener
import com.dscvit.vitty.network.api.community.RetrofitGhostActionListener
import com.dscvit.vitty.network.api.community.RetrofitJoinCircleListener
import com.dscvit.vitty.network.api.community.RetrofitUserActionListener
import com.dscvit.vitty.network.api.community.responses.circle.CircleRequestsResponse
import com.dscvit.vitty.network.api.community.responses.circle.CreateCircleResponse
import com.dscvit.vitty.network.api.community.responses.circle.JoinCircleResponse
import com.dscvit.vitty.network.api.community.responses.requests.RequestsResponse
import com.dscvit.vitty.network.api.community.responses.user.ActiveFriendResponse
import com.dscvit.vitty.network.api.community.responses.user.CircleResponse
import com.dscvit.vitty.network.api.community.responses.user.FriendResponse
import com.dscvit.vitty.network.api.community.responses.user.GhostPostResponse
import com.dscvit.vitty.network.api.community.responses.user.PostResponse
import com.dscvit.vitty.util.Constants
import com.google.gson.Gson
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
    private val _createCircleResponse = MutableLiveData<CreateCircleResponse?>()
    private val _joinCircleResponse = MutableLiveData<JoinCircleResponse?>()
    private val _joinCircleError = MutableLiveData<String?>()
    private val _isCircleLoading = MutableLiveData<Boolean>()
    private val _isCircleRefreshing = MutableLiveData<Boolean>()
    private val _circleMembers = MutableLiveData<Map<String, FriendResponse>>()
    private val _circleMembersLoading = MutableLiveData<Set<String>>()
    private val _receivedCircleRequests = MutableLiveData<CircleRequestsResponse?>()
    private val _sentCircleRequests = MutableLiveData<CircleRequestsResponse?>()
    private val _isCircleRequestsLoading = MutableLiveData<Boolean>()
    private val _circleActionResponse = MutableLiveData<PostResponse?>()
    private val _activeFriends = MutableLiveData<List<String>>()
    private val _ghostModeResponse = MutableLiveData<GhostModeResponse?>()

    val ghostModeResponse: MutableLiveData<GhostModeResponse?> = _ghostModeResponse
    val friendList: MutableLiveData<FriendResponse?> = _friendList
    val friendRequest: MutableLiveData<RequestsResponse?> = _friendRequest
    val requestActionResponse: MutableLiveData<PostResponse?> = _requestActionResponse
    val sendRequestResponse: MutableLiveData<PostResponse?> = _sendRequestResponse
    val isLoading: MutableLiveData<Boolean> = _isLoading
    val isRefreshing: MutableLiveData<Boolean> = _isRefreshing
    val unfriendSuccess: MutableLiveData<String?> = _unfriendSuccess
    val circleList: MutableLiveData<CircleResponse?> = _circleList
    val createCircleResponse: MutableLiveData<CreateCircleResponse?> = _createCircleResponse
    val joinCircleResponse: MutableLiveData<JoinCircleResponse?> = _joinCircleResponse
    val joinCircleError: MutableLiveData<String?> = _joinCircleError
    val isCircleLoading: MutableLiveData<Boolean> = _isCircleLoading
    val isCircleRefreshing: MutableLiveData<Boolean> = _isCircleRefreshing
    val circleMembers: MutableLiveData<Map<String, FriendResponse>> = _circleMembers
    val circleMembersLoading: MutableLiveData<Set<String>> = _circleMembersLoading
    val receivedCircleRequests: MutableLiveData<CircleRequestsResponse?> = _receivedCircleRequests
    val sentCircleRequests: MutableLiveData<CircleRequestsResponse?> = _sentCircleRequests
    val isCircleRequestsLoading: MutableLiveData<Boolean> = _isCircleRequestsLoading
    val circleActionResponse: MutableLiveData<PostResponse?> = _circleActionResponse
    val activeFriends: MutableLiveData<List<String>> = _activeFriends

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
                        _requestActionResponse.postValue(null)
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
                        _requestActionResponse.postValue(null)
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
        _circleMembers.postValue(emptyMap())
        _circleMembersLoading.postValue(emptySet())

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

                        response?.data?.forEach { circle ->
                            getCircleDetails(token, circle.circle_id)
                        }
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
        val currentLoading = _circleMembersLoading.value?.toMutableSet() ?: mutableSetOf()
        currentLoading.add(circleId)
        _circleMembersLoading.postValue(currentLoading)

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

                        val updatedLoading =
                                _circleMembersLoading.value?.toMutableSet() ?: mutableSetOf()
                        updatedLoading.remove(circleId)
                        _circleMembersLoading.postValue(updatedLoading)
                    }

                    override fun onError(
                            call: Call<FriendResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("CircleDetailsError for $circleId: ${t?.message}")

                        val updatedLoading =
                                _circleMembersLoading.value?.toMutableSet() ?: mutableSetOf()
                        updatedLoading.remove(circleId)
                        _circleMembersLoading.postValue(updatedLoading)
                    }
                },
        )
    }

    fun createCircle(
            token: String,
            circleName: String,
    ) {
        Timber.d(
                "ConnectViewModel.createCircle called with token: ${if (token.isNotEmpty()) "exists" else "empty"}, circleName: $circleName",
        )

        APICommunityRestClient.instance.createCircle(
                token,
                circleName,
                object : RetrofitCreateCircleListener {
                    override fun onSuccess(
                            call: Call<CreateCircleResponse>?,
                            response: CreateCircleResponse?,
                    ) {
                        Timber.d("CreateCircle: $response")
                        _createCircleResponse.postValue(response)
                    }

                    override fun onError(
                            call: Call<CreateCircleResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("CreateCircle Error: ${t?.message}")
                        _createCircleResponse.postValue(null)
                    }
                },
        )
    }

    fun joinCircleByCode(
            token: String,
            joinCode: String,
    ) {
        Timber.d(
                "ConnectViewModel.joinCircleByCode called with token: ${if (token.isNotEmpty()) "exists" else "empty"}, joinCode: $joinCode",
        )

        _joinCircleResponse.postValue(null)
        _joinCircleError.postValue(null)

        APICommunityRestClient.instance.joinCircleByCode(
                token,
                joinCode,
                object : RetrofitJoinCircleListener {
                    override fun onSuccess(
                            call: Call<JoinCircleResponse>?,
                            response: JoinCircleResponse?,
                    ) {
                        Timber.d("JoinCircle Success: $response")
                        _joinCircleResponse.postValue(response)
                    }

                    override fun onError(
                            call: Call<JoinCircleResponse>?,
                            t: Throwable?,
                    ) {
                        val errorMessage = t?.message ?: "Unknown error occurred"
                        Timber.d("JoinCircle Error: $errorMessage")
                        _joinCircleError.postValue(errorMessage)
                    }
                },
        )
    }

    fun clearCreateCircleResponse() {
        _createCircleResponse.postValue(null)
    }

    fun clearJoinCircleResponse() {
        _joinCircleResponse.postValue(null)
    }

    fun clearJoinCircleError() {
        _joinCircleError.postValue(null)
    }

    fun getReceivedCircleRequests(token: String) {
        _isCircleRequestsLoading.postValue(true)
        APICommunityRestClient.instance.getReceivedCircleRequests(
                token,
                object : RetrofitCircleRequestListener {
                    override fun onSuccess(
                            call: Call<CircleRequestsResponse>?,
                            response: CircleRequestsResponse?,
                    ) {
                        Timber.d("ReceivedCircleRequests: $response")
                        _receivedCircleRequests.postValue(response)
                        _isCircleRequestsLoading.postValue(false)
                    }

                    override fun onError(
                            call: Call<CircleRequestsResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("ReceivedCircleRequestsError: ${t?.message}")
                        _receivedCircleRequests.postValue(null)
                        _isCircleRequestsLoading.postValue(false)
                    }
                },
        )
    }

    fun getSentCircleRequests(token: String) {
        _isCircleRequestsLoading.postValue(true)
        APICommunityRestClient.instance.getSentCircleRequests(
                token,
                object : RetrofitCircleRequestListener {
                    override fun onSuccess(
                            call: Call<CircleRequestsResponse>?,
                            response: CircleRequestsResponse?,
                    ) {
                        Timber.d("SentCircleRequests: $response")
                        _sentCircleRequests.postValue(response)
                        _isCircleRequestsLoading.postValue(false)
                    }

                    override fun onError(
                            call: Call<CircleRequestsResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("SentCircleRequestsError: ${t?.message}")
                        _sentCircleRequests.postValue(null)
                        _isCircleRequestsLoading.postValue(false)
                    }
                },
        )
    }

    fun refreshCircleRequests(token: String) {
        getReceivedCircleRequests(token)
        getSentCircleRequests(token)
    }

    fun deleteCircle(
            token: String,
            circleId: String,
    ) {
        APICommunityRestClient.instance.deleteCircle(
                token,
                circleId,
                object : RetrofitUserActionListener {
                    override fun onSuccess(
                            call: Call<PostResponse>?,
                            response: PostResponse?,
                    ) {
                        Timber.d("DeleteCircle: $response")
                        _circleActionResponse.postValue(response)
                    }

                    override fun onError(
                            call: Call<PostResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("DeleteCircleError: ${t?.message}")
                        _circleActionResponse.postValue(null)
                    }
                },
        )
    }

    fun leaveCircle(
            token: String,
            circleId: String,
    ) {
        APICommunityRestClient.instance.leaveCircle(
                token,
                circleId,
                object : RetrofitUserActionListener {
                    override fun onSuccess(
                            call: Call<PostResponse>?,
                            response: PostResponse?,
                    ) {
                        Timber.d("LeaveCircle: $response")
                        _circleActionResponse.postValue(response)
                    }

                    override fun onError(
                            call: Call<PostResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("LeaveCircleError: ${t?.message}")
                        _circleActionResponse.postValue(null)
                    }
                },
        )
    }

    fun clearCircleActionResponse() {
        _circleActionResponse.postValue(null)
    }

    fun acceptCircleRequest(
            token: String,
            circleId: String,
    ) {
        APICommunityRestClient.instance.acceptCircleRequest(
                token,
                circleId,
                object : RetrofitUserActionListener {
                    override fun onSuccess(
                            call: Call<PostResponse>?,
                            response: PostResponse?,
                    ) {
                        Timber.d("AcceptCircleRequest: $response")
                        _circleActionResponse.postValue(response)
                    }

                    override fun onError(
                            call: Call<PostResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("AcceptCircleRequestError: ${t?.message}")
                        _circleActionResponse.postValue(null)
                    }
                },
        )
    }

    fun declineCircleRequest(
            token: String,
            circleId: String,
    ) {
        APICommunityRestClient.instance.declineCircleRequest(
                token,
                circleId,
                object : RetrofitUserActionListener {
                    override fun onSuccess(
                            call: Call<PostResponse>?,
                            response: PostResponse?,
                    ) {
                        Timber.d("DeclineCircleRequest: $response")
                        _circleActionResponse.postValue(response)
                    }

                    override fun onError(
                            call: Call<PostResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("DeclineCircleRequestError: ${t?.message}")
                        _circleActionResponse.postValue(null)
                    }
                },
        )
    }

    fun unsendCircleRequest(
            token: String,
            circleId: String,
            username: String,
    ) {
        APICommunityRestClient.instance.unsendCircleRequest(
                token,
                circleId,
                username,
                object : RetrofitUserActionListener {
                    override fun onSuccess(
                            call: Call<PostResponse>?,
                            response: PostResponse?,
                    ) {
                        Timber.d("UnsendCircleRequest: $response")
                        _circleActionResponse.postValue(response)
                    }

                    override fun onError(
                            call: Call<PostResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("UnsendCircleRequestError: ${t?.message}")
                        _circleActionResponse.postValue(null)
                    }
                },
        )
    }

    fun removeUserFromCircle(
            token: String,
            circleId: String,
            username: String,
    ) {
        APICommunityRestClient.instance.removeUserFromCircle(
                token,
                circleId,
                username,
                object : RetrofitUserActionListener {
                    override fun onSuccess(
                            call: Call<PostResponse>?,
                            response: PostResponse?,
                    ) {
                        Timber.d("RemoveUserFromCircle: $response")
                        _circleActionResponse.postValue(response)

                        getCircleDetails(token, circleId)
                    }

                    override fun onError(
                            call: Call<PostResponse>?,
                            t: Throwable?,
                    ) {
                        Timber.d("RemoveUserFromCircleError: ${t?.message}")
                        _circleActionResponse.postValue(null)
                    }
                },
        )
    }

    fun fetchActiveFriends(
            token: String,
            prefs: SharedPreferences,
    ) {
        APICommunityRestClient.instance.getActiveFriends(
                token,
                object : RetrofitActiveFriendsListener {
                    override fun onSuccess(
                            call: Call<ActiveFriendResponse>,
                            response: ActiveFriendResponse?,
                    ) {
                        val list = response?.data ?: emptyList()
                        _activeFriends.postValue(list)

                        prefs.edit {
                            putBoolean(Constants.ACTIVE_FRIENDS_FETCHED, true)
                                    .putString(Constants.ACTIVE_FRIENDS_LIST, Gson().toJson(list))
                        }
                    }

                    override fun onError(
                            call: Call<ActiveFriendResponse>,
                            t: Throwable,
                    ) {
                        _activeFriends.postValue(emptyList())
                    }
                },
        )
    }

    fun toggleGhostMode(
            token: String,
            username: String,
            enable: Boolean,
    ) {
        _ghostModeResponse.postValue(null)
        val api = APICommunityRestClient.instance
        if (enable) {
            api.enableGhostMode(
                    token,
                    username,
                    object : RetrofitGhostActionListener {
                        override fun onSuccess(
                                call: Call<GhostPostResponse>?,
                                response: GhostPostResponse?,
                        ) {
                            val success =
                                    response?.data?.contains("hidden", true) == true ||
                                            response?.data?.contains("visible", true) == true
                            _ghostModeResponse.postValue(GhostModeResponse(success))
                        }

                        override fun onError(
                                call: Call<GhostPostResponse>?,
                                t: Throwable?,
                        ) {
                            _ghostModeResponse.postValue(GhostModeResponse(false))
                        }
                    },
            )
        } else {
            api.disableGhostMode(
                    token,
                    username,
                    object : RetrofitGhostActionListener {
                        override fun onSuccess(
                                call: Call<GhostPostResponse>?,
                                response: GhostPostResponse?,
                        ) {
                            val success =
                                    response?.data?.contains("hidden", true) == true ||
                                            response?.data?.contains("visible", true) == true
                            _ghostModeResponse.postValue(GhostModeResponse(success))
                        }

                        override fun onError(
                                call: Call<GhostPostResponse>?,
                                t: Throwable?,
                        ) {
                            _ghostModeResponse.postValue(GhostModeResponse(false))
                        }
                    },
            )
        }
    }

    fun clearGhostModeResponse() {
        _ghostModeResponse.postValue(null)
    }

    fun updateActiveFriendsList(data: List<String>?) {
        _activeFriends.postValue(data)
    }

    data class GhostModeResponse(
            val success: Boolean,
    )
}
