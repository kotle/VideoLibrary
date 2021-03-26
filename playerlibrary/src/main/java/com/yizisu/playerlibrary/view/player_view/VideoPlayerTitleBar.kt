package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.PlayerModel

internal class VideoPlayerTitleBar : LinearLayout {
    var player:IYzsPlayer<PlayerModel>?=null
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        orientation = HORIZONTAL
        gravity=Gravity.CENTER_VERTICAL
    }

    private val backIv: View
    private val titleTv: TextView
    private val speedTv: TextView

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.video_player_title_bar, this, true)
        backIv = root.findViewById(R.id.playerBack)
        titleTv = root.findViewById(R.id.playerTitleTv)
        speedTv = root.findViewById(R.id.speedTv)
        speedTv.setOnClickListener {

        }
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String?) {
        titleTv.text = title
    }
}