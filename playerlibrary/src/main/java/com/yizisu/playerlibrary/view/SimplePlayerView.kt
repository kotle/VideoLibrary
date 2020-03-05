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
import android.widget.SeekBar
import android.widget.TextView
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.*
import com.yizisu.playerlibrary.helper.GestureDetectorHelper
import com.yizisu.playerlibrary.helper.adjustVolume
import com.yizisu.playerlibrary.helper.logI
import kotlinx.android.synthetic.main.layout_simple_player_view.view.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SimplePlayerView : FrameLayout {

    private val gestureDetectorHelper by lazy { GestureDetectorHelper(this, false) }
    /**
     * 手指离开屏幕处理操作
     */
    private var hideUiRunnable: Runnable? = null


    private fun getHideRunable(): Runnable? {
        if (hideUiRunnable != null) {
            removeCallbacks(hideUiRunnable)
        }
        hideUiRunnable = Runnable {
            isShowFull = true
            hideUiView()
        }
        return hideUiRunnable
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
    //横向拖动进度条监听
    private var onSeekListener: Function2<Boolean/*是否拖动完成*/,
            Float/*拖动的比例*/, Unit>? = null
    //本地拖动完成的长度
    private var seekLenght = 0f
    //视频总长度
    private var totalVideoDuration: Long = 0L
    //当前长度
    private var currentVideoDuration: Long = 0L
    //有值得时候代表是在手动拖动进度条，不允许再对进度条复制
    private var oldTouchSeekBarProgress: Int? = null
    private val mAudioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

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
                    val touchX = e1?.x ?: return@setOnScrollListener
                    val touchY = e1.y
                    if (checkTouchArea(touchX, touchY)) {
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
        }
        gestureDetectorHelper.setOnClickListener(OnClickListener {
            if (!isShowFull) {
                showUiView()
            } else {
                hideUiView()
            }
        })
        playerBack.setOnClickListener {
            if (context is Activity) {
                (context as Activity).finish()
            }
        }
        isShowFull = true
        postDelayed(getHideRunable(), 3000)
    }

    /**
     * 检查是否在允许滑动区域
     * 由于全面屏手势，边缘部分不允许响应手势
     */
    private fun checkTouchArea(touchX: Float, touchY: Float): Boolean {
        val offX = width.toFloat() / 20
        val offY = height.toFloat() / 10
        return touchX > offX && touchX < (width - offX)
                && touchY > offY && touchY < (height - offY)
    }

    /**
     * 横向滑动，调节进度
     */
    private fun adjustProgress(e1: MotionEvent?, e2: MotionEvent?, x: Float, y: Float) {
        seekLenght += x
        if (totalVideoDuration > 0) {
            lightView.visibility = View.VISIBLE
            if (seekLenght > 0) {
                lightView.text = "快退\n-${getMsByVideoDuration(seekLenght, width)}"
            } else {
                lightView.text = "快进\n+${getMsByVideoDuration(seekLenght, width)}"
            }
        }
        onSeekListener?.invoke(false, seekLenght / width)
    }

    private fun getMsByVideoDuration(seekLenght: Float, width: Int): String {
        return getCountTimeByLong(((abs(seekLenght) * totalVideoDuration) / (width * 1000)).toLong() * 1000L)
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
     * 显示界面操作
     */
    private fun showUiView() {
        if (!isShowFull) {
            setDisplayInNotch()
            isShowFull = true
            startViewAnim(progressLl, 0f)
            startViewAnim(topLl, 0f)
        }
    }

    /**
     * 隐藏界面操作
     */
    private fun hideUiView() {
        if (height == 0) {
            return
        }
        setDisplayInNotch()
        if (isPlaying && isShowFull) {
            isShowFull = false
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
        seekComplete(seekLenght / width)
    }

    private fun seekComplete(ratio: Float) {
        //托动完成
        if (ratio != 0f) {
            if (scrollOrientation == LinearLayout.HORIZONTAL) {
                onSeekListener?.invoke(true, ratio)
            }
            if (totalVideoDuration > 0) {
                setProgress(
                    (-totalVideoDuration * ratio).toLong() + currentVideoDuration,
                    null, totalVideoDuration
                )
            }
        }
        seekLenght = 0f
        scrollOrientation = null
        oldTouchSeekBarProgress = null
        clearAdjustVolume()
        lightView.visibility = View.GONE
        if (isPlaying) {
            postDelayed(getHideRunable(), 5000)
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

    fun setProgress(
        currentProgress: Long?,
        bufferProgress: Long?,
        allProgress: Long
    ) {
        val max = progressBar.max
        if (currentProgress != null) {
            currentVideoDuration = currentProgress
            currentProgressTv.text = getCountTimeByLong(currentProgress)
            if (oldTouchSeekBarProgress == null) {
                progressBar.progress = (currentProgress * max / allProgress).toInt()
            }
        }
        if (bufferProgress != null) {
            progressBar.secondaryProgress = (bufferProgress * max / allProgress).toInt()
        }
        totalVideoDuration = allProgress
        totalProgressTv.text = getCountTimeByLong(allProgress)
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
    fun setVideoSize(videoWidth: Int, videoHeight: Int, portraitScreen: Boolean) {
        textureView.layoutParams = if (portraitScreen) {
            changePlayerSize(
                min(width, height),
                max(width, height),
                videoWidth, videoHeight
            )
        } else {
            changePlayerSize(
                max(width, height),
                min(width, height),
                videoWidth, videoHeight
            )
        }
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
        if (isPlaying == isPlay) {
            return
        }
        eNPlayView.setDuration(500)
        if (isPlay) {
            isPlaying = true
            eNPlayView.play()
            hideUiView()
        } else {
            isPlaying = false
            eNPlayView.pause()
            showUiView()
        }
    }

    /**
     * 设置进度条回调
     */
    fun setOnSeekBarListener(l: Function2<Boolean, Float, Unit>) {
        onSeekListener = l
        progressBar.setOnSeekBarChangeListener(onSeekChange)
    }


    private val onSeekChange = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val oldProgress = oldTouchSeekBarProgress
            if (fromUser && totalVideoDuration > 0 && oldProgress != null) {
                lightView.visibility = View.VISIBLE
                val seekLenght = (oldProgress - progress).toFloat()
                if (seekLenght > 0) {
                    lightView.text = "快退\n-${getMsByVideoDuration(seekLenght, progressBar.max)}"
                } else {
                    lightView.text = "快进\n+${getMsByVideoDuration(seekLenght, progressBar.max)}"
                }
                onSeekListener?.invoke(false, seekLenght / progressBar.max)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            removeCallbacks(hideUiRunnable)
            oldTouchSeekBarProgress = progressBar.progress
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            val oldProgress = oldTouchSeekBarProgress
            if (oldProgress != null) {
                scrollOrientation = LinearLayout.HORIZONTAL
                seekComplete((oldProgress - progressBar.progress).toFloat() / progressBar.max)
            }
        }
    }

}