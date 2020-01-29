package com.iven.vectorify

data class VectorifyWallpaper(
    val backgroundColor: Int,
    val vectorColor: Int,
    val resource: Int,
    val category: Int,
    val scale: Float,
    val horizontalOffset: Float,
    val verticalOffset: Float
)
