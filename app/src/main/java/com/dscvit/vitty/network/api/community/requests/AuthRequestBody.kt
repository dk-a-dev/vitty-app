package com.dscvit.vitty.network.api.community.requests

data class AuthRequestBodyWithCampus(
    val reg_no: String,
    val username: String,
    val uuid: String,
    val campus: String,
)

data class AuthRequestBodyWithoutCampus(
    val reg_no: String,
    val username: String,
    val uuid: String,
)
