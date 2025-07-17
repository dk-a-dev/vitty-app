package com.dscvit.vitty.network.api.community

import com.dscvit.vitty.network.api.community.requests.UsernameRequestBody
import com.dscvit.vitty.network.api.community.responses.circle.CreateCircleResponse
import com.dscvit.vitty.network.api.community.responses.requests.RequestsResponse
import com.dscvit.vitty.network.api.community.responses.timetable.TimetableResponse
import com.dscvit.vitty.network.api.community.responses.user.CircleResponse
import com.dscvit.vitty.network.api.community.responses.user.FriendResponse
import com.dscvit.vitty.network.api.community.responses.user.PostResponse
import com.dscvit.vitty.network.api.community.responses.user.SignInResponse
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface APICommunity {
    @Headers("Content-Type: application/json")
    @POST("/api/v2/auth/check-username")
    fun checkUsername(
        @Body body: UsernameRequestBody,
    ): Call<PostResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v2/auth/firebase/")
    fun signInInfo(
        @Body body: Any,
    ): Call<SignInResponse>

    @GET("/api/v2/users/{username}")
    fun getUser(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<UserResponse>

    @GET("/api/v2/timetable/{username}/")
    fun getTimeTable(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<TimetableResponse>

    @GET("/api/v2/circles/{circleId}/{username}/")
    fun getCircleTimeTable(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
        @Path("username") username: String,
    ): Call<TimetableResponse>

    @GET("/api/v2/friends/{username}/")
    fun getFriendList(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<FriendResponse>

    @GET("/api/v2/users/search")
    fun searchUsers(
        @Header("Authorization") authToken: String,
        @Query("query") query: String,
    ): Call<List<UserResponse>>

    @GET("/api/v2/requests/")
    fun getFriendRequests(
        @Header("Authorization") authToken: String,
    ): Call<RequestsResponse>

    @GET("/api/v2/users/suggested/")
    fun getSuggestedFriends(
        @Header("Authorization") authToken: String,
    ): Call<List<UserResponse>>

    @POST("/api/v2/requests/{username}/send")
    fun sendRequest(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @POST("/api/v2/requests/{username}/accept/")
    fun acceptRequest(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @POST("/api/v2/requests/{username}/decline/")
    fun declineRequest(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @DELETE("/api/v2/friends/{username}/")
    fun deleteFriend(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @POST("/api/v2/friends/ghost/{username}")
    fun enableGhostMode(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @POST("/api/v2/friends/alive/{username}")
    fun disableGhostMode(
        @Header("Authorization") authToken: String,
        @Path("username") username: String,
    ): Call<PostResponse>

    @GET("/api/v2/circles")
    fun getCircles(
        @Header("Authorization") authToken: String,
    ): Call<CircleResponse>

    @POST("/api/v2/circles/create/{circleName}")
    fun createCircle(
        @Header("Authorization") authToken: String,
        @Path("circleName") circleName: String,
    ): Call<CreateCircleResponse>

    @GET("/api/v2/circles/{circleId}")
    fun getCircleDetails(
        @Header("Authorization") authToken: String,
        @Path("circleId") circleId: String,
    ): Call<FriendResponse>
}
