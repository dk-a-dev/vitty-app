package com.dscvit.vitty.network.api.community

import com.dscvit.vitty.network.api.community.requests.AuthRequestBodyWithCampus
import com.dscvit.vitty.network.api.community.requests.AuthRequestBodyWithoutCampus
import com.dscvit.vitty.network.api.community.requests.CampusUpdateRequestBody
import com.dscvit.vitty.network.api.community.requests.CircleBatchRequestBody
import com.dscvit.vitty.network.api.community.requests.UsernameRequestBody
import com.dscvit.vitty.network.api.community.responses.circle.CircleBatchRequestResponse
import com.dscvit.vitty.network.api.community.responses.circle.CircleRequestsResponse
import com.dscvit.vitty.network.api.community.responses.circle.CreateCircleResponse
import com.dscvit.vitty.network.api.community.responses.circle.JoinCircleResponse
import com.dscvit.vitty.network.api.community.responses.requests.RequestsResponse
import com.dscvit.vitty.network.api.community.responses.timetable.TimetableResponse
import com.dscvit.vitty.network.api.community.responses.user.ActiveFriendResponse
import com.dscvit.vitty.network.api.community.responses.user.CircleResponse
import com.dscvit.vitty.network.api.community.responses.user.FriendResponse
import com.dscvit.vitty.network.api.community.responses.user.GhostPostResponse
import com.dscvit.vitty.network.api.community.responses.user.PostResponse
import com.dscvit.vitty.network.api.community.responses.user.SignInResponse
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class APICommunityRestClient {
    companion object {
        val instance = APICommunityRestClient()
    }

    private var mApiUser: APICommunity? = null
    private val retrofit = CommunityNetworkClient.retrofitClientCommunity

    fun signInWithUsernameRegNo(
        username: String,
        regno: String,
        uuid: String,
        campus: String,
        retrofitCommunitySignInListener: RetrofitCommunitySignInListener,
        retrofitSelfUserListener: RetrofitSelfUserListener,
    ) {
        mApiUser = retrofit.create(APICommunity::class.java)

        val requestBody =
            if (campus != "") {
                AuthRequestBodyWithCampus(
                    reg_no = regno,
                    username = username,
                    uuid = uuid,
                    campus = campus,
                )
            } else {
                AuthRequestBodyWithoutCampus(
                    reg_no = regno,
                    username = username,
                    uuid = uuid,
                )
            }

        val apiSignInCall = mApiUser!!.signInInfo(requestBody)

        apiSignInCall.enqueue(
            object : Callback<SignInResponse> {
                override fun onResponse(
                    call: Call<SignInResponse>,
                    response: Response<SignInResponse>,
                ) {
                    retrofitCommunitySignInListener.onSuccess(call, response.body())
                    val token = response.body()?.token.toString()
                    val res_username = response.body()?.username.toString()

                    getUserWithTimeTable(token, res_username, retrofitSelfUserListener)
                }

                override fun onFailure(
                    call: Call<SignInResponse>,
                    t: Throwable,
                ) {
                    retrofitCommunitySignInListener.onError(call, t)
                }
            },
        )
    }

    fun getUserWithTimeTable(
        token: String,
        username: String,
        retrofitSelfUserListener: RetrofitSelfUserListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiUserCall = mApiUser!!.getUser(bearerToken, username)
        apiUserCall.enqueue(
            object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>,
                ) {
                    Timber.d("UserResponse: $response")
                    retrofitSelfUserListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<UserResponse>,
                    t: Throwable,
                ) {
                    Timber.e("Error fetching user with timetable: ${t.message}")
                    retrofitSelfUserListener.onError(call, t)
                }
            },
        )
    }

    fun getTimeTable(
        token: String,
        username: String,
        retrofitTimeTableListener: RetrofitTimetableListener,
    ) {
        val token = "Bearer $token"

        mApiUser = retrofit.create(APICommunity::class.java)
        val apiTimetableCall = mApiUser!!.getTimeTable(token, username)
        apiTimetableCall.enqueue(
            object : Callback<TimetableResponse> {
                override fun onResponse(
                    call: Call<TimetableResponse>,
                    response: Response<TimetableResponse>,
                ) {
                    retrofitTimeTableListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<TimetableResponse>,
                    t: Throwable,
                ) {
                    retrofitTimeTableListener.onError(call, t)
                }
            },
        )
    }

    fun getCircleTimeTable(
        token: String,
        circleId: String,
        username: String,
        retrofitTimeTableListener: RetrofitTimetableListener,
    ) {
        val token = "Bearer $token"

        mApiUser = retrofit.create(APICommunity::class.java)
        val apiCircleTimetableCall = mApiUser!!.getCircleTimeTable(token, circleId, username)
        apiCircleTimetableCall.enqueue(
            object : Callback<TimetableResponse> {
                override fun onResponse(
                    call: Call<TimetableResponse>,
                    response: Response<TimetableResponse>,
                ) {
                    retrofitTimeTableListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<TimetableResponse>,
                    t: Throwable,
                ) {
                    retrofitTimeTableListener.onError(call, t)
                }
            },
        )
    }

    fun getFriendList(
        token: String,
        username: String,
        retrofitFriendListListener: RetrofitFriendListListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiFriendListCall = mApiUser!!.getFriendList(bearerToken, username)
        apiFriendListCall.enqueue(
            object : Callback<FriendResponse> {
                override fun onResponse(
                    call: Call<FriendResponse>,
                    response: Response<FriendResponse>,
                ) {
                    Timber.d("FriendListResponse: ${response.body()}")
                    retrofitFriendListListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<FriendResponse>,
                    t: Throwable,
                ) {
                    retrofitFriendListListener.onError(call, t)
                }
            },
        )
    }

    fun getCircles(
        token: String,
        retrofitCircleListener: RetrofitCircleListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiCirclesCall = mApiUser!!.getCircles(bearerToken)
        apiCirclesCall.enqueue(
            object : Callback<CircleResponse> {
                override fun onResponse(
                    call: Call<CircleResponse>,
                    response: Response<CircleResponse>,
                ) {
                    retrofitCircleListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<CircleResponse>,
                    t: Throwable,
                ) {
                    retrofitCircleListener.onError(call, t)
                }
            },
        )
    }

    fun createCircle(
        token: String,
        circleName: String,
        retrofitCreateCircleListener: RetrofitCreateCircleListener,
    ) {
        val bearerToken = "Bearer $token"

        Timber.d("APICommunityRestClient.createCircle called with circleName: $circleName")

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiCreateCircleCall = mApiUser!!.createCircle(bearerToken, circleName)

        Timber.d("API call created, enqueueing request...")

        apiCreateCircleCall.enqueue(
            object : Callback<CreateCircleResponse> {
                override fun onResponse(
                    call: Call<CreateCircleResponse>,
                    response: Response<CreateCircleResponse>,
                ) {
                    Timber.d("API Response received: ${response.code()}, body: ${response.body()}")
                    retrofitCreateCircleListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<CreateCircleResponse>,
                    t: Throwable,
                ) {
                    Timber.e("API Call failed: ${t.message}")
                    retrofitCreateCircleListener.onError(call, t)
                }
            },
        )
    }

    fun joinCircleByCode(
        token: String,
        joinCode: String,
        retrofitJoinCircleListener: RetrofitJoinCircleListener,
    ) {
        val bearerToken = "Bearer $token"

        Timber.d("APICommunityRestClient.joinCircleByCode called with joinCode: $joinCode")

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiJoinCircleCall = mApiUser!!.joinCircleByCode(bearerToken, joinCode)

        Timber.d("Join circle API call created, enqueueing request...")

        apiJoinCircleCall.enqueue(
            object : Callback<JoinCircleResponse> {
                override fun onResponse(
                    call: Call<JoinCircleResponse>,
                    response: Response<JoinCircleResponse>,
                ) {
                    Timber.d("Join Circle API Response received: ${response.code()}, body: ${response.body()}")
                    if (response.isSuccessful) {
                        retrofitJoinCircleListener.onSuccess(call, response.body())
                    } else {
                        // Handle error responses (400, 409, 500)
                        val errorMessage =
                            when (response.code()) {
                                400 -> "Invalid join code"
                                409 -> "You are already part of the circle"
                                500 -> "Failed to join circle"
                                else -> "Unknown error occurred"
                            }
                        Timber.e("Join Circle API Error: ${response.code()} - $errorMessage")
                        retrofitJoinCircleListener.onError(call, Throwable(errorMessage))
                    }
                }

                override fun onFailure(
                    call: Call<JoinCircleResponse>,
                    t: Throwable,
                ) {
                    Timber.e("Join Circle API Call failed: ${t.message}")
                    retrofitJoinCircleListener.onError(call, t)
                }
            },
        )
    }

    fun getCircleDetails(
        token: String,
        circleId: String,
        retrofitFriendListListener: RetrofitFriendListListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiCircleDetailsCall = mApiUser!!.getCircleDetails(bearerToken, circleId)
        apiCircleDetailsCall.enqueue(
            object : Callback<FriendResponse> {
                override fun onResponse(
                    call: Call<FriendResponse>,
                    response: Response<FriendResponse>,
                ) {
                    retrofitFriendListListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<FriendResponse>,
                    t: Throwable,
                ) {
                    retrofitFriendListListener.onError(call, t)
                }
            },
        )
    }

    fun getSearchResult(
        token: String,
        query: String,
        retrofitSearchResultListener: RetrofitSearchResultListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiSearchResultCall = mApiUser!!.searchUsers(bearerToken, query)
        apiSearchResultCall.enqueue(
            object : Callback<List<UserResponse>> {
                override fun onResponse(
                    call: Call<List<UserResponse>>,
                    response: Response<List<UserResponse>>,
                ) {
                    Timber.d("SearchResult4: $response")
                    retrofitSearchResultListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<List<UserResponse>>,
                    t: Throwable,
                ) {
                    retrofitSearchResultListener.onError(call, t)
                }
            },
        )
    }

    fun getSuggestedFriends(
        token: String,
        retrofitSearchResultListener: RetrofitSearchResultListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiSuggestedResultCall = mApiUser!!.getSuggestedFriends(bearerToken)
        apiSuggestedResultCall.enqueue(
            object : Callback<List<UserResponse>> {
                override fun onResponse(
                    call: Call<List<UserResponse>>,
                    response: Response<List<UserResponse>>,
                ) {
                    Timber.d("SearchResult4: $response")
                    Timber.d("Response Code: ${response.code()}")
                    Timber.d("Response Headers: ${response.headers()}")
                    Timber.d("Response Body: ${response.body()}")
                    Timber.d("Response Raw: ${response.raw()}")
                    Timber.d("Response isSuccessful: ${response.isSuccessful}")

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Timber.d("Suggested Friends Count: ${body.size}")
                            retrofitSearchResultListener.onSuccess(call, body)
                        } else {
                            Timber.d("Response body is null, treating as empty list")
                            retrofitSearchResultListener.onSuccess(call, emptyList())
                        }
                    } else {
                        Timber.d("Response not successful: ${response.code()}")
                        retrofitSearchResultListener.onError(call, Exception("HTTP ${response.code()}"))
                    }
                }

                override fun onFailure(
                    call: Call<List<UserResponse>>,
                    t: Throwable,
                ) {
                    Timber.d("API Call Failed: ${t.message}")
                    retrofitSearchResultListener.onError(call, t)
                }
            },
        )
    }

    fun getFriendRequest(
        token: String,
        retrofitFriendRequestListener: RetrofitFriendRequestListener,
    ) {
        val bearerToken = "Bearer $token"
        Timber.d("FriendReqToken--: $bearerToken")
        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiFriendRequestCall = mApiUser!!.getFriendRequests(bearerToken)
        apiFriendRequestCall.enqueue(
            object : Callback<RequestsResponse> {
                override fun onResponse(
                    call: Call<RequestsResponse>,
                    response: Response<RequestsResponse>,
                ) {
                    Timber.d("FriendRequest--: $response")
                    retrofitFriendRequestListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<RequestsResponse>,
                    t: Throwable,
                ) {
                    Timber.d("FriendRequestError--: ${t.message}")
                    retrofitFriendRequestListener.onError(call, t)
                }
            },
        )
    }

    fun acceptRequest(
        token: String,
        username: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiAcceptRequestCall = mApiUser!!.acceptRequest(bearerToken, username)
        apiAcceptRequestCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun rejectRequest(
        token: String,
        username: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiRejectRequestCall = mApiUser!!.declineRequest(bearerToken, username)
        apiRejectRequestCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun sendRequest(
        token: String,
        username: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiSendRequestCall = mApiUser!!.sendRequest(bearerToken, username)
        apiSendRequestCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun unfriend(
        token: String,
        username: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiUnfriendCall = mApiUser!!.deleteFriend(bearerToken, username)
        apiUnfriendCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun sendCircleRequest(
        token: String,
        circleId: String,
        username: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create(APICommunity::class.java)
        val apiSendCircleRequestCall = mApiUser!!.sendCircleRequest(bearerToken, circleId, username)
        apiSendCircleRequestCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    Timber.d("SendCircleResponse: $response")
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun sendBatchCircleRequest(
        token: String,
        circleId: String,
        usernames: List<String>,
        callback: (CircleBatchRequestResponse?) -> Unit,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create(APICommunity::class.java)
        val requestBody = CircleBatchRequestBody(usernames)
        val apiSendBatchCircleRequestCall = mApiUser!!.sendBatchCircleRequest(bearerToken, circleId, requestBody)
        apiSendBatchCircleRequestCall.enqueue(
            object : Callback<CircleBatchRequestResponse> {
                override fun onResponse(
                    call: Call<CircleBatchRequestResponse>,
                    response: Response<CircleBatchRequestResponse>,
                ) {
                    Timber.d("SendBatchCircleResponse: $response")
                    callback(response.body())
                }

                override fun onFailure(
                    call: Call<CircleBatchRequestResponse>,
                    t: Throwable,
                ) {
                    Timber.d("SendBatchCircleRequestError: ${t.message}")
                    callback(null)
                }
            },
        )
    }

    fun checkUsername(
        username: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val usernameRequestBody = UsernameRequestBody(username)
        val apiCheckUsernameCall = mApiUser!!.checkUsername(usernameRequestBody)
        apiCheckUsernameCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    if (response.isSuccessful) {
                        retrofitUserActionListener.onSuccess(call, response.body())
                    } else {
                        val gson = Gson()
                        val errorString = response.errorBody()?.string()
                        Timber.d("ResponseV: $errorString")
                        try {
                            val errorResponse = gson.fromJson(errorString, PostResponse::class.java)
                            retrofitUserActionListener.onSuccess(call, errorResponse)
                        } catch (e: JsonSyntaxException) {
                            // Handle any JSON parsing errors.
                            val errorMessage = "Username is not valid/available."
                            retrofitUserActionListener.onSuccess(call, PostResponse(errorMessage))
                        }
                    }
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    Timber.d("ErrorV: ${t.message}")
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun enableGhostMode(
        token: String,
        username: String,
        retrofitUserActionListener: RetrofitGhostActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiGhostModeCall = mApiUser!!.enableGhostMode(bearerToken, username)
        apiGhostModeCall.enqueue(
            object : Callback<GhostPostResponse> {
                override fun onResponse(
                    call: Call<GhostPostResponse>,
                    response: Response<GhostPostResponse>,
                ) {
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<GhostPostResponse>,
                    t: Throwable,
                ) {
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun disableGhostMode(
        token: String,
        username: String,
        retrofitUserActionListener: RetrofitGhostActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiGhostModeCall = mApiUser!!.disableGhostMode(bearerToken, username)
        apiGhostModeCall.enqueue(
            object : Callback<GhostPostResponse> {
                override fun onResponse(
                    call: Call<GhostPostResponse>,
                    response: Response<GhostPostResponse>,
                ) {
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<GhostPostResponse>,
                    t: Throwable,
                ) {
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun getReceivedCircleRequests(
        token: String,
        retrofitCircleRequestListener: RetrofitCircleRequestListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiReceivedCircleRequestsCall = mApiUser!!.getReceivedCircleRequests(bearerToken)
        apiReceivedCircleRequestsCall.enqueue(
            object : Callback<CircleRequestsResponse> {
                override fun onResponse(
                    call: Call<CircleRequestsResponse>,
                    response: Response<CircleRequestsResponse>,
                ) {
                    Timber.d("ReceivedCircleRequests: ${response.body()}")
                    retrofitCircleRequestListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<CircleRequestsResponse>,
                    t: Throwable,
                ) {
                    Timber.d("ReceivedCircleRequestsError: ${t.message}")
                    retrofitCircleRequestListener.onError(call, t)
                }
            },
        )
    }

    fun getSentCircleRequests(
        token: String,
        retrofitCircleRequestListener: RetrofitCircleRequestListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiSentCircleRequestsCall = mApiUser!!.getSentCircleRequests(bearerToken)
        apiSentCircleRequestsCall.enqueue(
            object : Callback<CircleRequestsResponse> {
                override fun onResponse(
                    call: Call<CircleRequestsResponse>,
                    response: Response<CircleRequestsResponse>,
                ) {
                    Timber.d("SentCircleRequests: ${response.body()}")
                    retrofitCircleRequestListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<CircleRequestsResponse>,
                    t: Throwable,
                ) {
                    Timber.d("SentCircleRequestsError: ${t.message}")
                    retrofitCircleRequestListener.onError(call, t)
                }
            },
        )
    }

    fun deleteCircle(
        token: String,
        circleId: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiDeleteCircleCall = mApiUser!!.deleteCircle(bearerToken, circleId)
        apiDeleteCircleCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    Timber.d("DeleteCircle: ${response.body()}")
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    Timber.d("DeleteCircleError: ${t.message}")
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun leaveCircle(
        token: String,
        circleId: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiLeaveCircleCall = mApiUser!!.leaveCircle(bearerToken, circleId)
        apiLeaveCircleCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    Timber.d("LeaveCircle: ${response.body()}")
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    Timber.d("LeaveCircleError: ${t.message}")
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun acceptCircleRequest(
        token: String,
        circleId: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiAcceptCircleRequestCall = mApiUser!!.acceptCircleRequest(bearerToken, circleId)
        apiAcceptCircleRequestCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    Timber.d("AcceptCircleRequest: ${response.body()}")
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    Timber.d("AcceptCircleRequestError: ${t.message}")
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun declineCircleRequest(
        token: String,
        circleId: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiDeclineCircleRequestCall = mApiUser!!.declineCircleRequest(bearerToken, circleId)
        apiDeclineCircleRequestCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    Timber.d("DeclineCircleRequest: ${response.body()}")
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    Timber.d("DeclineCircleRequestError: ${t.message}")
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun unsendCircleRequest(
        token: String,
        circleId: String,
        username: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiUnsendCircleRequestCall = mApiUser!!.unsendCircleRequest(bearerToken, circleId, username)
        apiUnsendCircleRequestCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    Timber.d("UnsendCircleRequest: ${response.body()}")
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    Timber.d("UnsendCircleRequestError: ${t.message}")
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun getEmptyClassrooms(
        token: String,
        slot: String,
        callback: (Map<String, List<String>>?) -> Unit,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiEmptyClassroomsCall = mApiUser!!.getEmptyClassrooms(bearerToken, slot)
        apiEmptyClassroomsCall.enqueue(
            object : Callback<Map<String, List<String>>> {
                override fun onResponse(
                    call: Call<Map<String, List<String>>>,
                    response: Response<Map<String, List<String>>>,
                ) {
                    Timber.d("EmptyClassrooms: ${response.body()}")
                    callback(response.body())
                }

                override fun onFailure(
                    call: Call<Map<String, List<String>>>,
                    t: Throwable,
                ) {
                    Timber.d("EmptyClassroomsError: ${t.message}")
                    callback(null)
                }
            },
        )
    }

    fun removeUserFromCircle(
        token: String,
        circleId: String,
        username: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val apiRemoveUserCall = mApiUser!!.removeUserFromCircle(bearerToken, circleId, username)
        apiRemoveUserCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    Timber.d("RemoveUserFromCircle: ${response.body()}")
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    Timber.d("RemoveUserFromCircleError: ${t.message}")
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun updateCampus(
        token: String,
        campus: String,
        retrofitUserActionListener: RetrofitUserActionListener,
    ) {
        val bearerToken = "Bearer $token"

        mApiUser = retrofit.create<APICommunity>(APICommunity::class.java)
        val requestBody = CampusUpdateRequestBody(campus)
        val apiUpdateCampusCall = mApiUser!!.updateCampus(bearerToken, requestBody)
        apiUpdateCampusCall.enqueue(
            object : Callback<PostResponse> {
                override fun onResponse(
                    call: Call<PostResponse>,
                    response: Response<PostResponse>,
                ) {
                    Timber.d("UpdateCampus: ${response.body()}")
                    retrofitUserActionListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<PostResponse>,
                    t: Throwable,
                ) {
                    Timber.d("UpdateCampusError: ${t.message}")
                    retrofitUserActionListener.onError(call, t)
                }
            },
        )
    }

    fun getActiveFriends(
        token: String,
        retrofitActiveFriendsListener: RetrofitActiveFriendsListener,
    ) {
        val bearerToken = "Bearer $token"
        mApiUser = retrofit.create(APICommunity::class.java)
        val apiActiveFriendsCall = mApiUser!!.getActiveFriends(bearerToken)
        apiActiveFriendsCall.enqueue(
            object : Callback<ActiveFriendResponse> {
                override fun onResponse(
                    call: Call<ActiveFriendResponse>,
                    response: Response<ActiveFriendResponse>,
                ) {
                    Timber.d("ActiveFriends: ${response.body()}")
                    retrofitActiveFriendsListener.onSuccess(call, response.body())
                }

                override fun onFailure(
                    call: Call<ActiveFriendResponse>,
                    t: Throwable,
                ) {
                    Timber.d("ActiveFriendsError: ${t.message}")
                    retrofitActiveFriendsListener.onError(call, t)
                }
            },
        )
    }
}
