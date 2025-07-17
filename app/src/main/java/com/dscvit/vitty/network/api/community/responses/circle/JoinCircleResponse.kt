package com.dscvit.vitty.network.api.community.responses.circle

data class JoinCircleResponse(
    val detail: String
)

data class JoinCircleError(
    val detail: String? = null,
    val error: String? = null
)
