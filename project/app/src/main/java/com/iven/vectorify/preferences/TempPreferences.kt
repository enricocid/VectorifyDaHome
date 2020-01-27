package com.iven.vectorify.preferences

import android.graphics.Color
import com.iven.vectorify.utils.Utils

class TempPreferences {

    var tempBackgroundColor = Color.BLACK
    var tempVectorColor = Color.WHITE
    var tempVector = Utils.getDefaultVectorForApi()
    var tempCategory = 0
    var tempScale = 0.35F
    var tempHorizontalOffset = 0F
    var tempVerticalOffset = 0F
}
