package com.iven.vectorify.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VectorifyWallpaper(
    val backgroundColor: Int,
    val vectorColor: Int,
    val resource: Int,
    val category: Int,
    val scale: Float,
    val horizontalOffset: Float,
    val verticalOffset: Float
)
