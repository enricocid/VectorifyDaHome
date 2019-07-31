package com.iven.vectorify.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView

//lol
class CircleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        radius = width / 2f
        strokeWidth = width / 10
    }
}