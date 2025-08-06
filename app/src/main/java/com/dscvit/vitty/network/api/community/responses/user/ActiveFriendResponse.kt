package com.dscvit.vitty.network.api.community.responses.user

data class ActiveFriendResponse(
    val data: List<ActiveFriendItem>?,
)

data class ActiveFriendItem(
    val friend_username: String?,
    val hide: Boolean?,
)
