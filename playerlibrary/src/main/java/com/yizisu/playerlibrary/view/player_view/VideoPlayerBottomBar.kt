package com.yizisu.playerlibrary.view.player_view

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
    private val ivFull: ImageView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        LayoutInflater.from(context).inflate(R.layout.video_player_bottom_bar, this, true)
        playOrPauseView = findViewById(R.id.eNPlayClickView)
        progressBar = findViewById(R.id.progressBar)
        currentProgressTv = findViewById(R.id.currentProgressTv)
        totalProgressTv = findViewById(R.id.totalProgressTv)
        ivFull = findViewById(R.id.ivFull)
        seekBarHelper = SeekBarHelper(progressBar, ::onSeekCompelete)
        playOrPauseView.enableGoneWhenPlaying = false
        playOrPauseView.visibility = View.VISIBLE
        playOrPauseView.eNPlayView.setLineWidth(dip(1f))
        playOrPauseView.eNPlayView.setBgLineWidth(dip(1f))
        ivFull.setOnClickListener {
            val old = isFullScreenData.value ?: false
            isFullScreenData.value = !old
        }
    }

    /**
     * 当前状态是否全屏
     */
    val isFullScreenData = MutableLiveData(false)

    private val observeFullScreen = Observer<Boolean> {
        if (!it) {
            ivFull.setImageResource(R.drawable.exo_ic_fullscreen_enter)
        } else {
            ivFull.setImageResource(R.drawable.exo_ic_fullscreen_exit)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullScreenData.observeForever(observeFullScreen)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isFullScreenData.removeObserver(observeFullScreen)
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

    private fun needHour(allProgress: Long): Boolean {
        return allProgress > 1 * 60 * 60 * 1000
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
            currentProgressTv.text = getCountTimeByLong(currentProgress, needHour(allProgress))
            val progress = (currentProgress * max / allProgress).toInt()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                progressBar.setProgress(progress, true)
            } else {
                progressBar.progress = progress
            }
        }
        if (bufferProgress != null && allProgress != 0L) {
            progressBar.secondaryProgress = (bufferProgress * max / allProgress).toInt()
        }
        totalProgressTv.text = getCountTimeByLong(allProgress)
    }

    private inner class SeekBarHelper(bar: SeekBar, onSeekCompleteListener: Function1<Float, Unit>) {
        var isTouchSeekBar = false
        private var currentProgressRatio: Float? = null

        init {
            bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                ) {
                    if (fromUser) {
                        player?.getCurrentModel()?.apply {
                            val f = progress.toFloat() / bar.max
                            currentProgressRatio = f
                            currentProgressTv.text = getCountTimeByLong((f * totalDuration).toLong(), needHour(totalDuration))
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isTouchSeekBar = true
                    currentProgressRatio = null
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isTouchSeekBar = false
                    currentProgressRatio?.let {
                        onSeekCompleteListener.invoke(it)
                    }
                }
            })
        }
    }
}