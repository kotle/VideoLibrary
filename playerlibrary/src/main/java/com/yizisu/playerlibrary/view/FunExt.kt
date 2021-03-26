package com.yizisu.playerlibrary.view

import android.util.TypedValue
import android.view.View
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener

internal fun View.dip(value: Float): Float {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics)
}

internal fun View.dip(value: Int): Int {
    return dip(value.toFloat()).toInt()
}

internal fun autoBindListener(
    value: IYzsPlayer<PlayerModel>?,
    field: IYzsPlayer<PlayerModel>?,
    l: SimplePlayerListener<PlayerModel>
) {
    if (value == null) {
        field?.removePlayerListener(l)
    } else {
        value.addPlayerListener(l)
    }
}