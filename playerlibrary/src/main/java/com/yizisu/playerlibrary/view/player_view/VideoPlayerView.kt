package com.yizisu.playerlibrary.view.player_view

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.helper.fullScreen
import com.yizisu.playerlibrary.view.autoBindListener
import com.yizisu.playerlibrary.view.dip

/**
 * 上方的标题栏
 */
class VideoPlayerView : FrameLayout, SimplePlayerListener<PlayerModel> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    //手势控制view
    private val gestureView = VideoPlayerGestureView(context).apply {
        //监听是否隐藏view
        setShowOrHideBarListener(true) { show, animPercentage ->
            if (!show) {
                //隐藏
                titleBar.translationY = -titleBar.height * animPercentage
                bottomBar.translationY = bottomBar.height * animPercentage
            } else {
                //显示
                titleBar.translationY = -titleBar.height * (1 - animPercentage)
                bottomBar.translationY = bottomBar.height * (1 - animPercentage)
                if (bottomBar.visibility != View.VISIBLE && player?.getCurrentModel() != null) {
                    bottomBar.visibility = View.VISIBLE
                }
            }
        }
    }

    //标题栏view
    private val titleBar = VideoPlayerTitleBar(context).apply {

    }

    //底部栏view
    private val bottomBar = VideoPlayerBottomBar(context).apply {
        this.visibility = View.INVISIBLE
    }

    //自动播放暂停view
    private val autoPlayView = AutoPlayOrPauseView(context).apply {
        visibility = View.GONE
        setPadding(dip(24), dip(24), dip(24), dip(24))
    }

    //画面显示view
    private val textureView = VideoPlayerTexture(context)

    //加载的view
    private val loadingView = ProgressBar(context).apply {
        visibility = View.GONE
    }

    /**
     * 是否全屏
     */
    val isFullScreenData: MutableLiveData<Boolean>
        get() = bottomBar.isFullScreenData

    /**
     * 触摸事件监听
     */
    val touchEventData: MutableLiveData<MotionEvent?>
        get() = gestureView.touchEventData

    init {
        setBackgroundColor(Color.BLACK)
        //必须添加
        addView(textureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        //选填
        addPlayerView()
    }

    private fun addPlayerView() {
        if (gestureView.parent == null) {
            addView(gestureView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
        if (loadingView.parent == null) {
            addView(loadingView, LayoutParams(dip(32), dip(32)).apply {
                gravity = Gravity.CENTER
            })
        }
        if (autoPlayView.parent == null) {
            addView(autoPlayView, LayoutParams(dip(80), dip(80)).apply {
                gravity = Gravity.CENTER
            })
        }
        if (titleBar.parent == null) {
            addView(
                titleBar,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.TOP
                })
        }
        if (bottomBar.parent == null) {
            addView(
                bottomBar,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.BOTTOM
                })
        }
    }

    /**
     * 绑定播放器
     */
    private var player: IYzsPlayer<PlayerModel>? = null
    fun attachPlayer(player: IYzsPlayer<PlayerModel>?) {
        autoBindListener(player, this.player, this)
        this.player = player
        gestureView.player = player
        autoPlayView.player = player
        textureView.player = player
        bottomBar.player = player
        titleBar.player = player
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String?) {
        titleBar.setTitle(title)
    }

    override fun onTick(playerModel: PlayerModel) {

    }

    override fun onPrepare(playerModel: PlayerModel?) {
        super.onPrepare(playerModel)
        val title = playerModel?.getTitle()
        if (!title.isNullOrBlank()) {
            setTitle(title.toString())
        }
    }

    override fun onBufferStateChange(
        isBuffering: Boolean,
        playStatus: Boolean,
        playerModel: PlayerModel?
    ) {
        super.onBufferStateChange(isBuffering, playStatus, playerModel)
        if (playStatus) {
            if (isBuffering) {
                loadingView.visibility = View.VISIBLE
            } else {
                loadingView.visibility = View.GONE
            }
        } else {
            loadingView.visibility = View.GONE
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                //触摸屏幕的时候，取消隐藏动作
                gestureView.removeHideUiRunnable()
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> {
                gestureView.postHideUi()
            }
            else -> {
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onPlay(playStatus: Boolean, playerModel: PlayerModel?) {
        super.onPlay(playStatus, playerModel)
        keepScreenOn = playStatus
    }

    override fun onPause(playStatus: Boolean, playerModel: PlayerModel?) {
        super.onPause(playStatus, playerModel)
        keepScreenOn = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullScreenData.observeForever(::onFullScreenChange)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isFullScreenData.removeObserver(::onFullScreenChange)
    }

    private fun onFullScreenChange(b: Boolean?) {
        if (b == true) {
            if (resources.displayMetrics.widthPixels > resources.displayMetrics.heightPixels) {
                setPadding(dip(32), 0, dip(40), 0)
            } else {
                setPadding(0, dip(24), 0, dip(24))
            }
        } else {
            setPadding(0, 0, 0, 0)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        onFullScreenChange(isFullScreenData.value)
    }
}