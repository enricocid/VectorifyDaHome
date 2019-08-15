package com.iven.vectorify.preferences

import android.graphics.Color
import com.iven.vectorify.R

class TempPreferences {

    var tempBackgroundColor = Color.BLACK
    var tempVectorColor = Color.WHITE
    var tempVector = R.drawable.android
    var tempScale = 0.35F

    var isBackgroundColorChanged = false
    var isVectorColorChanged = false
    var isVectorChanged = false
    var isScaleChanged = false
}
