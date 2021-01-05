package com.yizisu.videolibrary

import android.content.Context
import android.util.AttributeSet
import com.yizisu.playerlibrary.view.SimplePlayerView

class MyPlayerView : SimplePlayerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        super.hideUiView()
    }

    override fun showUiView() {
//        super.showUiView()
    }

    override fun hideUiView() {
//        super.hideUiView()
    }
}