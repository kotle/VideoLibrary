package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.view.RatioLayout
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
            }
        }
    }

    //标题栏view
    private val titleBar = VideoPlayerTitleBar(context).apply {

    }

    //底部栏view
    private val bottomBar = VideoPlayerBottomBar(context).apply {

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
}