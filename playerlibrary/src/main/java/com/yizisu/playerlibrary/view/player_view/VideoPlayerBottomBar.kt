package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.helper.getCountTimeByLong
import com.yizisu.playerlibrary.view.autoBindListener
import com.yizisu.playerlibrary.view.dip

internal class VideoPlayerBottomBar : LinearLayout, SimplePlayerListener<PlayerModel> {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val seekBarHelper: SeekBarHelper
    private val playOrPauseView: AutoPlayOrPauseView
    private val progressBar: SeekBar
    private val currentProgressTv: TextView
    private val totalProgressTv: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        LayoutInflater.from(context).inflate(R.layout.video_player_bottom_bar, this, true)
        playOrPauseView = findViewById(R.id.eNPlayClickView)
        progressBar = findViewById(R.id.progressBar)
        currentProgressTv = findViewById(R.id.currentProgressTv)
        totalProgressTv = findViewById(R.id.totalProgressTv)
        seekBarHelper = SeekBarHelper(progressBar, ::onSeekCompelete)
        playOrPauseView.enableGoneWhenPlaying = false
        playOrPauseView.visibility = View.VISIBLE
        playOrPauseView.eNPlayView.setLineWidth(dip(1f))
        playOrPauseView.eNPlayView.setBgLineWidth(dip(1f))
    }


    var player: IYzsPlayer<PlayerModel>? = null
        set(value) {
            autoBindListener(value, field, this)
            playOrPauseView.player = value
            field = value
        }

    override fun onTick(playerModel: PlayerModel) {
        setProgress(
            playerModel.currentDuration,
            playerModel.currentBufferDuration,
            playerModel.totalDuration
        )
    }


    override fun onPrepare(playerModel: PlayerModel?) {
        super.onPrepare(playerModel)
        currentProgressTv.text = getCountTimeByLong(0)
        totalProgressTv.text = getCountTimeByLong(0)
        progressBar.progress = 0
        progressBar.secondaryProgress = 0
    }

    /**
     * 滑动进度条完成
     */
    private fun onSeekCompelete(fl: Float) {
        player?.seekRatioTo(fl)
    }

    /**
     * 设置进度
     */

    fun setProgress(
        currentProgress: Long?,
        bufferProgress: Long?,
        allProgress: Long
    ) {
        if (seekBarHelper.isTouchSeekBar) {
            //滑动进度条，不允许更改进度
            return
        }
        val max = progressBar.max
        if (currentProgress != null && allProgress != 0L) {
            currentProgressTv.text = getCountTimeByLong(currentProgress)
            progressBar.progress = (currentProgress * max / allProgress).toInt()
        }
        if (bufferProgress != null && allProgress != 0L) {
            progressBar.secondaryProgress = (bufferProgress * max / allProgress).toInt()
        }
        totalProgressTv.text = getCountTimeByLong(allProgress)
    }

    private class SeekBarHelper(bar: SeekBar, onSeekCompleteListener: Function1<Float, Unit>) {
        var isTouchSeekBar = false

        init {
            bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isTouchSeekBar = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isTouchSeekBar = false
                    onSeekCompleteListener.invoke(bar.progress.toFloat() / bar.max)
                }
            })
        }
    }
}