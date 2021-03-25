package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.TextureView
import android.widget.FrameLayout
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.view.RatioLayout
import com.yizisu.playerlibrary.view.dip

/**
 * 上方的标题栏
 */
class VideoPlayerView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val gestureView = VideoPlayerGestureView(context)
    private val titleBar = VideoPlayerTitleBar(context)
    private val autoPlayView = AutoPlayOrPauseView(context).apply {
        setPadding(dip(24),dip(24),dip(24),dip(24))
    }
    private val ratioLayout = RatioLayout(context).apply {
        addView(TextureView(context))
    }

    init {
        addView(ratioLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(gestureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(autoPlayView, LayoutParams(dip(80), dip(80)).apply {
            gravity=Gravity.CENTER
        })
        addView(titleBar, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun attachPlayer(player:IYzsPlayer<PlayerModel>){

    }

}