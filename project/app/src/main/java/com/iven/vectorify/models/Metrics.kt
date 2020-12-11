package com.iven.vectorify.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)

data class Metrics(val width: Int, val height: Int)
