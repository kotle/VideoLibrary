package com.yizisu.playerlibrary.view

import android.util.TypedValue
import android.view.View

internal fun View.dip(value: Float): Float {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics)
}

internal fun View.dip(value: Int): Int {
    return dip(value.toFloat()).toInt()
}