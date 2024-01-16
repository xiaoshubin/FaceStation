package com.smallcake.temp.bean

data class FaceRequestItem(
    val image: String,
    val image_type: String,
    val face_type: String,
    val liveness_control: String,
    val quality_control: String
)