package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.view.ENPlayView
import com.yizisu.playerlibrary.view.dip

/**
 * 暂停的时候显示在界面上的view
 */
class AutoPlayOrPauseView : FrameLayout, SimplePlayerListener<PlayerModel> {
    //是否在播放的时候隐藏按钮
    var enableGoneWhenPlaying = true
    //播放器
    var player: IYzsPlayer<PlayerModel>? = null
        set(value) {
            field = value
            value?.addPlayerListener(this)
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val eNPlayView by lazy {
        ENPlayView(context).apply {
            setLineWidth(dip(1.6f))
            setBgLineWidth(dip(1.6f))
            addView(this, LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    init {
        setOnClickListener {
            val player = this.player
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }
    }

    override fun onTick(playerModel: PlayerModel) {
    }

    override fun onPlay(playStatus: Boolean, playerModel: PlayerModel?) {
        super.onPlay(playStatus, playerModel)
        if (playStatus) {
            eNPlayView.play {
                if (enableGoneWhenPlaying) {
                    postDelayed({
                        this.visibility = View.INVISIBLE
                    }, 200)
                }
            }
        }
    }

    override fun onPause(playStatus: Boolean, playerModel: PlayerModel?) {
        super.onPause(playStatus, playerModel)
        if (!playStatus) {//播放暂停
            this.visibility = View.VISIBLE
            eNPlayView.pause()
        }
    }
}