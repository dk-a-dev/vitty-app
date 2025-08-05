package com.dscvit.vitty.network.api.community.responses.circle

data class CircleBatchResponseItem(
    val request_status: String,
    val username: String,
)

data class CircleBatchRequestResponse(
    val data: List<CircleBatchResponseItem>,
)


data class CreateCircleRequest(
    val circleName: String
)
