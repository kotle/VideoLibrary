package com.yizisu.playerlibrary.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.*
import com.yizisu.playerlibrary.helper.GestureDetectorHelper
import com.yizisu.playerlibrary.helper.adjustVolume
import com.yizisu.playerlibrary.helper.logI
import kotlinx.android.synthetic.main.layout_simple_player_view.view.*
import kotlin.math.abs

class SimplePlayerView : FrameLayout {
    private val gestureDetectorHelper by lazy { GestureDetectorHelper(this, false) }
    /**
     * 手指离开屏幕处理操作
     */
    private val hideUiRunnable = Runnable {
        isShowFull = true
        hideUiView()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    val textureView: TextureView
        get() = simplePlayerTexture

    private val lightView: TextView
        get() = adjustLightTv

    private var isShowFull = true
    //当前是否正在播放
    private var isPlaying = false
    private var scrollOrientation: Int? = null

    init {
        View.inflate(context, R.layout.layout_simple_player_view, this)
        gestureDetectorHelper.setOnScrollListener { e1, e2, x, y ->
            when (scrollOrientation) {
                LinearLayout.VERTICAL -> {
                    adjustVolumeOrLight(e1, e2, x, y)
                }
                LinearLayout.HORIZONTAL -> {
                    adjustProgress(e1, e2, x, y)
                }
                else -> {
                    if (abs(x) > abs(y)) {
                        //横向滑动
                        scrollOrientation = LinearLayout.HORIZONTAL
                        adjustProgress(e1, e2, x, y)
                    } else {
                        //纵向滑动
                        scrollOrientation = LinearLayout.VERTICAL
                        adjustVolumeOrLight(e1, e2, x, y)
                    }
                }
            }
        }
        gestureDetectorHelper.setOnClickListener(OnClickListener {
            isShowFull = !isShowFull
            if (isShowFull) {
                hideUiView()
            } else {
                showUiView()
            }
        })
        playerBack.setOnClickListener {
            if (context is Activity) {
                (context as Activity).finish()
            }
        }
        post(hideUiRunnable)
    }

    /**
     * 横向滑动，调节进度
     */
    private fun adjustProgress(e1: MotionEvent?, e2: MotionEvent?, x: Float, y: Float) {

    }

    /**
     * 处理纵向滑动事件
     */
    private fun adjustVolumeOrLight(e1: MotionEvent?, e2: MotionEvent?, x: Float, y: Float) {
        val downEvent = e1 ?: return
        val moveEvent = e2 ?: return
        if (downEvent.x < width.toFloat() / 2) {
            logI("在左边滑动")
            setVolume(y)
        } else {
            logI("在右边滑动")
            setScreenLight(y)
        }
    }


    /**
     * 隐藏界面操作
     */
    private fun showUiView() {
        isShowFull = false
        setDisplayInNotch()
        startViewAnim(progressLl, 0f)
        startViewAnim(topLl, 0f)
    }

    /**
     * 显示界面操作
     */
    private fun hideUiView() {
        if (isPlaying && isShowFull) {
            removeCallbacks(hideUiRunnable)
            startViewAnim(progressLl, progressLl.height.toFloat())
            startViewAnim(topLl, -topLl.height.toFloat())
        }
    }

    private fun startViewAnim(view: View, y: Float) {
        view.animate().cancel()
        view.animate().setDuration(360)
            .translationY(y)
            .start()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            actionUp()
        }
        return gestureDetectorHelper.onTouchEvent(event)
    }


    private fun actionUp() {
        scrollOrientation = null
        clearAdjustVolume()
        lightView.visibility = View.GONE
        if (isPlaying && isShowFull) {
            postDelayed(hideUiRunnable, 3000)
        }
    }


    /**
     * 设置视频尺寸
     */
    private fun changePlayerSize(
        viewWidth: Int, viewHeight: Int,
        videoWidth: Int, videoHeight: Int
    ): LayoutParams {
        val viewR = viewWidth.toFloat() / viewHeight
        val videoR = videoWidth.toFloat() / videoHeight
        val lp = if (viewR <= videoR) {
            //view的宽度不变设置高度
            LayoutParams(
                viewWidth, viewWidth * videoHeight / videoWidth
            )
        } else {
            //view高度不变,动态设置宽度
            LayoutParams(
                viewHeight * videoWidth / videoHeight, viewHeight
            )
        }
        lp.gravity = Gravity.CENTER
        return lp
    }

    /**
     * 调节亮度
     */
    private val mAudioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    @SuppressLint("SetTextI18n")
    private fun setScreenLight(offY: Float) {
        lightView.visibility = View.VISIBLE
        val currentLight = setScreenBrightnessSlide(offY / height)
        lightView.text = "亮度\n${(currentLight * 100).toInt()}"
    }

    @SuppressLint("SetTextI18n")
    private fun setVolume(offY: Float) {
        lightView.visibility = View.VISIBLE
        val currentLight = adjustVolume(mAudioManager, offY)
        lightView.text = "音量\n${currentLight}"
    }

    /***************************************************************************************/

    /**
     * 设置进度
     */
    fun setProgress(t1: String, t2: String, progress: Int, secondPro: Int) {
        currentProgressTv.text = t1
        totalProgressTv.text = t2
        progressBar.progress = progress
        progressBar.secondaryProgress = secondPro
    }

    /**
     * 设置视频信息
     */
    fun setVideoInfo(title: String?) {
        playerTitleTv?.text = title
    }

    /**
     * 设置视频尺寸
     */
    fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        textureView.layoutParams = changePlayerSize(
            width,
            height,
            videoWidth, videoHeight
        )
    }

    /**
     * 设置双击事件
     */
    fun setOnDoubleClickListener(l: View.OnClickListener?) {
        gestureDetectorHelper.setOnDoubleClickListener(l)
        eNPlayClickView.setOnClickListener {
            l?.onClick(it)
        }
    }

    /**
     * 设置播放按钮
     */

    fun setPlay(isPlay: Boolean) {
        eNPlayView.setDuration(500)
        if (isPlay) {
            isPlaying = true
            eNPlayView.play()
            actionUp()
        } else {
            isPlaying = false
            eNPlayView.pause()
            showUiView()
        }
    }
}