package com.iven.vectorify

class TempPreferences {

    var tempBackgroundColor = mVectorifyPreferences.backgroundColor
    var tempVectorColor = mVectorifyPreferences.vectorColor
    var tempIsBackgroundAccented = mVectorifyPreferences.isBackgroundAccented
    var tempIsVectorAccented = mVectorifyPreferences.isVectorAccented
    var tempVector = mVectorifyPreferences.vector
    var tempScale = mVectorifyPreferences.scale

    var isBackgroundColorChanged = false
    var isVectorColorChanged = false
    var isBackgroundAccentSet = false
    var isVectorAccentSet = false
    var isVectorChanged = false
    var isScaleChanged = false
}
