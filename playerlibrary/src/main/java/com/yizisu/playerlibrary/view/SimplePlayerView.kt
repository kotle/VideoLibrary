package com.yizisu.playerlibrary.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Point
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import android.os.Vibrator
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.PlayerLifecycleObserver
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.databinding.LayoutSimplePlayerViewBinding
import com.yizisu.playerlibrary.helper.*
import com.yizisu.playerlibrary.helper.GestureDetectorHelper
import com.yizisu.playerlibrary.helper.adjustVolume
import com.yizisu.playerlibrary.helper.logI
import kotlin.math.abs
import kotlin.math.min

class SimplePlayerView : FrameLayout, PlayerLifecycleObserver {
    private val binding by lazy {
        LayoutSimplePlayerViewBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.layout_simple_player_view, this, true)
        )
    }

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

    private val vibrator by lazy { context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    val textureView: TextureView
        get() = binding.simplePlayerTexture

    private val lightView: TextView
        get() = binding.adjustLightTv

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

    //手指滑动的视频总长度,从左滑到有，最多可以滑动进度
    //横屏十分钟，竖屏五分钟
    private val maxDurationPoint = Point(150_000, 300_000)
    private val swipeMaxWidth: Int
        get() = (width.toFloat() * totalVideoDuration / getSwipeMaxDuration()).toInt()

    //当前长度
    private var currentVideoDuration: Long = 0L

    //有值得时候代表是在手动拖动进度条，不允许再对进度条复制
    private var oldTouchSeekBarProgress: Int? = null
    private val speedList = mutableListOf(
        0.01f, 0.1f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2.0f
    )
    private var currentSpeedIndex = speedList.indexOf(1f)
    private var speedChangeListener: Function1<Float, Unit>? = null
    private val mAudioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    init {
        setBackgroundColor(Color.BLACK)
        getActivity()?.lifecycle?.addObserver(this)
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
        gestureDetectorHelper.setOnClickListener {
            if (!isShowFull) {
                showUiView()
            } else {
                hideUiView()
            }
        }
        gestureDetectorHelper.setOnLongClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                VibrationEffect.createOneShot(100, DEFAULT_AMPLITUDE)
            } else {
                vibrator?.vibrate(100)
            }
            binding.speedHintTv.visibility = View.VISIBLE
            speedChangeListener?.invoke(2f)
        }
        binding.playerBack.setOnClickListener {
            val activity = getActivity()
            if (activity != null) {
                if (isScreenPortrait()) {
                    activity.onBackPressed()
                } else {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            }
        }
        setSpeed(currentSpeedIndex)
        binding.speedTv.setOnClickListener {
            AlertDialog.Builder(context, R.style.Theme_AppCompat_Dialog)
                .setTitle("选择倍速")
                .setPositiveButton(android.R.string.cancel, null)
                .setAdapter(
                    ArrayAdapter<String>(
                        context,
                        android.R.layout.simple_list_item_1,
                        speedList.map { it.toString() }
                    )
                ) { dialog, which ->
                    setSpeed(which)
                    currentSpeedIndex = which
                }.show()
        }
        isShowFull = true
        postDelayed(getHideRunable(), 3000)
        /**
         * 设置全屏点击
         */
        binding.ivFull.setOnClickListener {
            val activity = getActivity()
            if (activity != null) {
                if (isScreenPortrait()) {
                    activity.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    activity.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            }
        }
        checkIcon()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        checkIcon()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        checkIcon()
    }

    private fun checkIcon() {
        if (isScreenPortrait()) {
            binding.ivFull.setImageResource(R.drawable.exo_ic_fullscreen_enter)
        } else {
            binding.ivFull.setImageResource(R.drawable.exo_ic_fullscreen_exit)
        }
    }

    private fun getActivity(): AppCompatActivity? {
        val ctx = context
        if (ctx is AppCompatActivity) {
            return ctx
        }
        if (ctx is ContextWrapper && ctx.baseContext is AppCompatActivity) {
            return ctx.baseContext as AppCompatActivity
        }
        return null
    }

    /**
     * 是否是竖屏
     */
    private fun isScreenPortrait(): Boolean {
        val ctx = getActivity()
        return if (ctx != null) {
            ctx.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    || ctx.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        } else {
            false
        }
    }

    override fun onStart() {
        super.onStart()
        if (textureView.visibility == View.VISIBLE) {
            textureView.visibility = View.INVISIBLE
            textureView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setSpeed(index: Int) {
        val i = index % speedList.count()
        val speed = speedList[i]
        binding.speedTv.text = "倍速 $speed"
        speedChangeListener?.invoke(speed)
        cancelHideRunnable()
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
                lightView.text = "快退\n-${getMsByVideoDuration(seekLenght, swipeMaxWidth)}"
            } else {
                lightView.text = "快进\n+${getMsByVideoDuration(seekLenght, swipeMaxWidth)}"
            }
            onSeekListener?.invoke(false, seekLenght / swipeMaxWidth)
        }
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
        if (downEvent.x > width.toFloat() / 2) {
            logI("在右边滑动")
            setVolume(y)
        } else {
            logI("在左边滑动")
            setScreenLight(y)
        }
    }

    /**
     * 获取可以滑动的最大长度
     * 横屏10分钟 竖屏5分钟
     *
     */
    private fun getSwipeMaxDuration(): Long {
        var ctx = context
        if (ctx is ContextThemeWrapper) {
            //是在对话框
            ctx = ctx.baseContext
        }
        return if (ctx is AppCompatActivity) {
            if (ctx.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                ctx.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            ) {
                //竖屏
                min(totalVideoDuration, maxDurationPoint.x.toLong())
            } else {
                //横屏
                min(totalVideoDuration, maxDurationPoint.y.toLong())
            }
        } else {
            min(totalVideoDuration, maxDurationPoint.y.toLong())
        }
    }

    /**
     * 显示界面操作
     */
    fun showUiView() {
        if (!isShowFull) {
            isShowFull = true
            startViewAnim(binding.progressLl, 0f)
            startViewAnim(binding.topLl, 0f)
        }
    }

    /**
     * 隐藏界面操作
     */
    fun hideUiView() {
        if (height == 0) {
            return
        }
        if (isPlaying && isShowFull) {
            isShowFull = false
            removeCallbacks(hideUiRunnable)
            startViewAnim(binding.progressLl, binding.progressLl.height.toFloat())
            startViewAnim(binding.topLl, -binding.topLl.height.toFloat())
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

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * 处理手指抬起
     */
    private fun actionUp() {
        //恢复倍速
        setSpeed(currentSpeedIndex)
        seekComplete(seekLenght / swipeMaxWidth)
    }

    /**
     * 进度改变完成
     */
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
        binding.speedHintTv.visibility = View.GONE
        if (isPlaying) {
            postDelayed(getHideRunable(), 5000)
        }
    }

    /**
     * 取消之前的隐藏操作
     */
    private fun cancelHideRunnable() {
        val runnable = getHideRunable()
        if (isPlaying) {
            postDelayed(runnable, 5000)
        }
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

    /**
     * 拖动seekbar监听
     */
    private var isTouchSeekBar = false
    private val onSeekChange = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val oldProgress = oldTouchSeekBarProgress
            if (fromUser && totalVideoDuration > 0 && oldProgress != null) {
                lightView.visibility = View.VISIBLE
                val seekLenght = (oldProgress - progress).toFloat()
                if (seekLenght > 0) {
                    lightView.text =
                        "快退\n-${getMsByVideoDuration(seekLenght, binding.progressBar.max)}"
                } else {
                    lightView.text =
                        "快进\n+${getMsByVideoDuration(seekLenght, binding.progressBar.max)}"
                }
                onSeekListener?.invoke(false, seekLenght / binding.progressBar.max)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            isTouchSeekBar = true
            removeCallbacks(hideUiRunnable)
            oldTouchSeekBarProgress = binding.progressBar.progress
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            isTouchSeekBar = false
            val oldProgress = oldTouchSeekBarProgress
            if (oldProgress != null) {
                scrollOrientation = LinearLayout.HORIZONTAL
                seekComplete((oldProgress - binding.progressBar.progress).toFloat() / binding.progressBar.max)
            }
        }
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
        if (isTouchSeekBar) {
            return
        }
        val max = binding.progressBar.max
        if (currentProgress != null && allProgress != 0L) {
            currentVideoDuration = currentProgress
            binding.currentProgressTv.text = getCountTimeByLong(currentProgress)
            if (oldTouchSeekBarProgress == null) {
                binding.progressBar.progress = (currentProgress * max / allProgress).toInt()
            }
        }
        if (bufferProgress != null && allProgress != 0L) {
            binding.progressBar.secondaryProgress = (bufferProgress * max / allProgress).toInt()
        }
        totalVideoDuration = allProgress
        binding.totalProgressTv.text = getCountTimeByLong(allProgress)
    }

    /**
     * 设置视频信息
     */
    fun setVideoInfo(title: String?) {
        binding.playerTitleTv.text = title
    }

    /**
     * 设置视频尺寸
     */
    fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        binding.ratioLayout.setChildRatio(videoWidth.toFloat() / videoHeight)
    }


    /**
     * 设置横竖屏下，最多可以滑动的视频时长
     * 单位毫秒
     * x横屏状态下的阈值
     * y竖屏状态下的阈值
     */
    fun setSwipeMaxDuration(x: Int, y: Int) {
        maxDurationPoint.set(x, y)
    }

    /**
     * 设置双击事件
     */
    fun setOnDoubleClickListener(l: Function1<MotionEvent?, Unit>?) {
        gestureDetectorHelper.setOnDoubleClickListener(l)
        binding.eNPlayClickView.setOnClickListener {
            l?.invoke(null)
        }
    }

    /**
     * 设置播放按钮
     */

    fun setPlay(isPlay: Boolean) {
        if (isPlaying == isPlay) {
            return
        }
        binding.eNPlayView.setDuration(500)
        if (isPlay) {
            isPlaying = true
            binding.eNPlayView.play()
            hideUiView()
        } else {
            isPlaying = false
            binding.eNPlayView.pause()
            showUiView()
        }
    }

    /**
     * 设置进度条回调
     */
    fun setOnSeekBarListener(l: Function2<Boolean, Float, Unit>) {
        onSeekListener = l
        binding.progressBar.setOnSeekBarChangeListener(onSeekChange)
    }

    /**
     * 设置改变倍速监听
     */
    fun setOnSpeedChangeListener(l: Function1<Float, Unit>) {
        speedChangeListener = l
    }


    fun <Model : PlayerModel> attachPlayer(player: IYzsPlayer<Model>): SimplePlayerListener<Model> {
        binding.retryTv.setOnClickListener {
            player.retry()
            binding.retryTv.visibility = View.GONE
        }
        setOnDoubleClickListener {
            if (player.isPlaying()) {
                player.pause()
            } else {
                player.play()
            }
            setPlay(player.isPlaying())
        }
        setOnSpeedChangeListener {
            player.setVideoSpeed(it)
        }
        setOnSeekBarListener { b, fl ->
            if (b) {//拖动完成
                player.getCurrentModel()?.let {
                    val total = it.totalDuration
                    val current = it.currentDuration - total * fl
                    player.seekTo(current.toLong())
                }
            }
        }
        player.attachView(textureView)
        val listener = object : SimplePlayerListener<Model> {
            override fun onTick(playerModel: Model) {
                setProgress(
                    playerModel.currentDuration,
                    playerModel.currentBufferDuration,
                    playerModel.totalDuration
                )
            }

            override fun onVideoSizeChange(
                width: Int,
                height: Int,
                unappliedRotationDegrees: Int,
                pixelWidthHeightRatio: Float,
                playerModel: Model?
            ) {
                setVideoSize(width, height)
            }

            override fun onPlay(playStatus: Boolean, playerModel: Model?) {
                super.onPlay(playStatus, playerModel)
                setPlay(playStatus)
            }

            override fun onPause(playStatus: Boolean, playerModel: Model?) {
                super.onPause(playStatus, playerModel)
                setPlay(playStatus)
                binding.loadingPb.visibility = View.GONE
            }

            override fun onBufferStateChange(
                isBuffering: Boolean,
                playStatus: Boolean,
                playerModel: Model?
            ) {
                super.onBufferStateChange(isBuffering, playStatus, playerModel)
                if (playStatus) {
                    if (isBuffering) {
                        binding.loadingPb.visibility = View.VISIBLE
                    } else {
                        binding.loadingPb.visibility = View.GONE
                    }
                } else {
                    setPlay(false)
                }
            }

            override fun onError(throwable: Throwable, playerModel: Model?) {
                super.onError(throwable, playerModel)
                binding.retryTv.visibility = View.VISIBLE
            }

            override fun onPlayerModelChange(playerModel: Model) {
                super.onPlayerModelChange(playerModel)
                binding.playerTitleTv.text = playerModel.getTitle()
            }
        }
        player.addPlayerListener(listener)
        return listener
    }
}