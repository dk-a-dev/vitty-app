package com.dscvit.vitty.network.api.community.responses.user

data class CircleResponse(
    val `data`: List<CircleItem>?,
)

data class CircleItem(
    val circle_id: String,
    val circle_name: String,
    val circle_role: String,
    val circle_join_code: String,
)
