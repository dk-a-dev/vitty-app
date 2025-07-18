package com.dscvit.vitty.network.api.community.responses.circle

data class CircleRequestItem(
    val circle_id: String,
    val circle_name: String,
    val from_username: String,
    val to_username: String,
)
