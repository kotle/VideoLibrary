package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.R

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

    private val backIv: View
    private val titleTv: TextView
    private val speedTv: TextView

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.video_player_title_bar, this, true)
        backIv = root.findViewById(R.id.playerBack)
        titleTv = root.findViewById(R.id.playerTitleTv)
        speedTv = root.findViewById(R.id.speedTv)
    }

    fun setTitle(title: String) {
        titleTv.text = title
    }

    fun attachPlayer(player: IYzsPlayer<*>) {
        speedTv.setOnClickListener {

        }
    }
}