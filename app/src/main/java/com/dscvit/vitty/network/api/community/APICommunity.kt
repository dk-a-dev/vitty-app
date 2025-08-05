package com.dscvit.vitty.network.api.community

import com.dscvit.vitty.network.api.community.requests.CampusUpdateRequestBody
import com.dscvit.vitty.network.api.community.requests.CircleBatchRequestBody
import com.dscvit.vitty.network.api.community.requests.UsernameRequestBody
import com.dscvit.vitty.network.api.community.responses.circle.CircleBatchRequestResponse
import com.dscvit.vitty.network.api.community.responses.circle.CircleRequestsResponse
import com.dscvit.vitty.network.api.community.responses.circle.CreateCircleRequest
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
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface APICommunity {
    @GET("/")
    fun checkServerStatus(): Call<String>

    @Headers("Content-Type: application/json")
    @POST("/api/v3/auth/check-username")
    fun checkUsername(
        @Body body: UsernameRequestBody,
    ): Call<PostResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v3/auth/firebase/")
    fun signInInfo(
        @Body body: Any,
    ): Call<SignInResponse>

    @GET("/api/v3/users/{username}")
    fun getUser(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<UserResponse>

    @GET("/api/v3/timetable/{username}/")
    fun getTimeTable(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<TimetableResponse>

    @GET("/api/v3/circles/{circleId}/{username}/")
    fun getCircleTimeTable(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
        @Path("username") username: String,
    ): Call<TimetableResponse>

    @GET("/api/v3/friends/{username}/")
    fun getFriendList(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<FriendResponse>

    @GET("/api/v3/users/search")
    fun searchUsers(
        @Header("Authorization") authToken: String,
        @Query("query") query: String,
    ): Call<List<UserResponse>>

    @GET("/api/v3/requests/")
    fun getFriendRequests(
        @Header("Authorization") authToken: String,
    ): Call<RequestsResponse>

    @GET("/api/v3/users/suggested/")
    fun getSuggestedFriends(
        @Header("Authorization") authToken: String,
    ): Call<List<UserResponse>>

    @POST("/api/v3/requests/{username}/send")
    fun sendRequest(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @POST("/api/v3/requests/{username}/accept/")
    fun acceptRequest(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @POST("/api/v3/requests/{username}/decline/")
    fun declineRequest(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @DELETE("/api/v3/friends/{username}/")
    fun deleteFriend(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @Headers("Content-Type: application/json")
    @PATCH("/api/v3/users/campus")
    fun updateCampus(
        @Header("Authorization") authToken: String,
        @Body campusRequestBody: CampusUpdateRequestBody,
    ): Call<PostResponse>

    @POST("/api/v3/friends/ghost/{username}")
    fun enableGhostMode(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<GhostPostResponse>

    @POST("/api/v3/friends/alive/{username}")
    fun disableGhostMode(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<GhostPostResponse>

    @GET("/api/v3/circles")
    fun getCircles(
        @Header("Authorization") authToken: String,
    ): Call<CircleResponse>

    @POST("/api/v3/circles/create")
    fun createCircle(
        @Header("Authorization") authToken: String,
        @Body requestBody: CreateCircleRequest //
    ): Call<CreateCircleResponse>

    @POST("/api/v3/circles/join")
    fun joinCircleByCode(
        @Header("Authorization") authToken: String,
        @Query("code") joinCode: String,
    ): Call<JoinCircleResponse>

    @GET("/api/v3/circles/{circleId}")
    fun getCircleDetails(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
    ): Call<FriendResponse>

    @POST("/api/v3/circles/sendRequest/{circleId}/{username}")
    fun sendCircleRequest(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v3/circles/sendRequest/{circleId}")
    fun sendBatchCircleRequest(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
        @Body body: CircleBatchRequestBody,
    ): Call<CircleBatchRequestResponse>

    @GET("/api/v3/circles/requests/received")
    fun getReceivedCircleRequests(
        @Header("Authorization") authToken: String,
    ): Call<CircleRequestsResponse>

    @GET("/api/v3/circles/requests/sent")
    fun getSentCircleRequests(
        @Header("Authorization") authToken: String,
    ): Call<CircleRequestsResponse>

    @DELETE("/api/v3/circles/{circleId}")
    fun deleteCircle(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
    ): Call<PostResponse>

    @DELETE("/api/v3/circles/leave/{circleId}")
    fun leaveCircle(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
    ): Call<PostResponse>

    @POST("/api/v3/circles/acceptRequest/{circleId}")
    fun acceptCircleRequest(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
    ): Call<PostResponse>

    @POST("/api/v3/circles/declineRequest/{circleId}")
    fun declineCircleRequest(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
    ): Call<PostResponse>

    @DELETE("/api/v3/circles/unsendRequest/{circleId}/{username}")
    fun unsendCircleRequest(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @DELETE("/api/v3/circles/remove/{circleId}/{username}")
    fun removeUserFromCircle(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @GET("/api/v3/timetable/emptyClassRooms")
    fun getEmptyClassrooms(
        @Header("Authorization") authToken: String,
        @Query("slot") slot: String,
    ): Call<Map<String, List<String>>>

    @GET(value = "/api/v3/friends/active")
    fun getActiveFriends(
        @Header("Authorization") authToken: String,
    ): Call<ActiveFriendResponse>
}
