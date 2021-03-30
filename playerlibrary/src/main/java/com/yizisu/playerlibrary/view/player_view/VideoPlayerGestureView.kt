package com.yizisu.playerlibrary.view.player_view


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.GestureDetectorHelper
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.view.dip

internal class VideoPlayerGestureView : FrameLayout {
    /**
     * 手势监听器对象
     */
    internal val gestureDetectorHelper by lazy { GestureDetectorHelper(this, false) }

    /**
     * 播放器对象
     */
    var player: IYzsPlayer<*>? = null

    /**
     * 操作栏是否正在展示
     */
    private var _isShow = false

    /**
     * 展示操作栏的监听
     */
    private var _showListener: ((show: Boolean, animPercentage: Float/*0-1*/) -> Unit)? = null

    /**
     * 显示操作栏的动画
     */
    private val animShow by lazy {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 285
            addUpdateListener {
                _showListener?.invoke(_isShow, it.animatedFraction)
            }
        }
    }

    /**
     * 当前倍速
     */
    private var _currentSpeed = 1f

    /**
     * 长按播放的倍速
     */
    private val longPressSpeed = 2f

    /**
     * 三秒后自动隐藏操作栏
     */
    private val autoHideUiDuration = 3000L

    private val hideUiRunnable = Runnable {
        if (_isShow) {
            switchShow()
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    )

    private val swipeViewHelper: SwipeViewHelper

    /**
     * 显示倍速播放的view
     */
    private val speedView by lazy {
        AppCompatTextView(context).apply {
            visibility = View.GONE
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.bg_dialog_adjust)
            val padding = dip(8)
            setPadding(padding, padding, padding, padding)
            //添加speedView
            this@VideoPlayerGestureView.addView(this, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                setMargins(0, dip(32), 0, 0)
            })
        }
    }
    private val midProgressHintView by lazy {
        MidProgressHintView(context).apply {
            visibility = View.INVISIBLE
            setBackgroundResource(R.drawable.bg_dialog_adjust)
            //添加speedView
            this@VideoPlayerGestureView.addView(this, LayoutParams(
                    dip(88),
                    dip(72)
            ).apply {
                gravity = Gravity.CENTER
            })
        }
    }

    init {
        gestureDetectorHelper.isLongpressEnabled = true
        gestureDetectorHelper.setOnClickListener {
            switchShow()
        }
        gestureDetectorHelper.setOnDoubleClickListener {
            playOrPause()
        }
        gestureDetectorHelper.setOnLongClickListener {
            setSpeed()
        }
        swipeViewHelper = SwipeViewHelper(this, midProgressHintView)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP
                || event?.action == MotionEvent.ACTION_CANCEL
        ) {
            actionUp()
        }
        return gestureDetectorHelper.onTouchEvent(event)
    }

    /**
     * 手指抬起
     */
    private fun actionUp() {
        val player = this.player
        midProgressHintView.visibility = View.GONE
        if (player != null) {
            if (player.getVideoSpeed() == longPressSpeed) {
                //当前是长按倍速播放
                speedView.visibility = View.GONE
                player.setVideoSpeed(_currentSpeed)
            }
        }
        //更新进度，里面自行判断是否需要更新
        swipeViewHelper.updateProgress()
    }

    /**
     * 开始倍速播放
     */
    private fun setSpeed() {
        if (speedView.visibility == View.VISIBLE) {
            //已经在倍速播放
            return
        }
        val player = this.player ?: return

        _currentSpeed = player.getVideoSpeed()
        if (_currentSpeed == longPressSpeed) {
            return
        }
        player.setVideoSpeed(longPressSpeed)
        speedView.visibility = View.VISIBLE
        speedView.text = "${longPressSpeed}X播放中"
    }

    /**
     * 播放或者暂停
     */
    private fun playOrPause() {
        val player = this.player ?: return
        if (player.isPlaying()) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (animShow.isRunning) {
            animShow.end()
        }
    }


    /**
     * 切换是否正在显示
     */
    private fun switchShow() {
        if (animShow.isRunning) {
            return
        }
        _isShow = !_isShow
        animShow.start()
        postHideUi()
    }

    /**
     * 设置展示或者隐藏操作栏监听
     */
    fun setShowOrHideBarListener(
            show: Boolean,
            listener: (show: Boolean, animPercentage: Float/*进度0-1f*/) -> Unit
    ) {
        _isShow = show
        _showListener = listener
        postHideUi()
    }

    /**
     * 延时隐藏操作栏
     */
    fun postHideUi() {
        removeHideUiRunnable()
        if (_isShow) {
            postDelayed(hideUiRunnable, autoHideUiDuration)
        }
    }

    /**
     * 移除延时隐藏操作栏
     */
    fun removeHideUiRunnable() {
        removeCallbacks(hideUiRunnable)
    }
}