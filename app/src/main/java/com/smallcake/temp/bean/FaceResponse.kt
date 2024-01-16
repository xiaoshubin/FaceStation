package com.smallcake.temp.bean

data class FaceResponse(
    val cached: Int,
    val error_code: Int,
    val error_msg: String,
    val log_id: Long,
    val result: Result,
    val timestamp: Int
)