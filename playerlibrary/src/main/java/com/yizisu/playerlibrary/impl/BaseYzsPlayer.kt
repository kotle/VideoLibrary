package com.yizisu.playerlibrary.impl

import android.content.Context
import android.media.AudioManager
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.SimplePlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.impl.exoplayer.mainHandler
import java.util.*

abstract class BaseYzsPlayer(private val context: Context) : IYzsPlayer {
    //当前播放模式，支持四种
    private var currentLoopMode = SimplePlayer.LOOP_MODO_NONE
    //这个索引，不经过各种判断直接赋值
    protected var _currentIndex = 0
    //设置当前索引
    var currentIndex
        get() = _currentIndex
        set(value) {
            val count = playModelList.count()
            _currentIndex = if (count == 0) {
                0
            } else {
                when (currentLoopMode) {
                    SimplePlayer.LOOP_MODO_SINGLE -> {
                        _currentIndex
                    }
                    SimplePlayer.LOOP_MODO_LIST -> {
                        if (value < 0) {
                            count - 1
                        } else {
                            if (value >= count) {
                                0
                            } else {
                                value
                            }
                        }
                    }
                    SimplePlayer.LOOP_MODO_SHUFF -> {
                        IntRange(0, count - 1).random()
                    }
                    else -> {
                        if (value < 0) {
                            0
                        } else {
                            if (value >= count) {
                                count - 1
                            } else {
                                value
                            }
                        }
                    }
                }
            }
        }

    //监听器集合
    private val playerListener = mutableListOf<SimplePlayerListener>()
    //定时任务
    private val timerTask = object : TimerTask() {
        override fun run() {
            if (playModelList.isNotEmpty()) {
                mainHandler.post {
                    currentPlayModel?.let { model ->
                        doPlayerListener {
                            model._totalDuration = totalDuration
                            model._currentDuration = currentDuration
                            model._currentBufferDuration = currentBufferDuration
                            it.onTick(model)
                        }
                    }
                }
            }
        }
    }
    private val timer = Timer()

    init {
        timer.schedule(timerTask, 0, 1000)
    }

    //是否支持处理音频焦点
    //若处理，则有焦点才播放，失去焦点停止播放
    private var audioFocusHelper: AudioFocusHelper? = null

    //当前播放列表集合
    protected val playModelList = mutableListOf<PlayerModel>()
    //当前播放的model
    val currentPlayModel: PlayerModel?
        get() {
            return if (currentIndex < playModelList.count() && currentIndex >= 0) {
                playModelList[currentIndex]
            } else {
                null
            }
        }
    //视频总时间
    open val totalDuration: Long = 0
    //当前播放时间
    open val currentDuration: Long = 0
    //当前已经缓存的时间
    open val currentBufferDuration: Long = 0


    override fun prepare(
        models: MutableList<PlayerModel>,
        playIndex: Int,
        isStopLastMedia: Boolean,
        listener: ((PlayerModel?) -> Unit)?
    ) {
        playModelList.clear()
        playModelList.addAll(models)
        _currentIndex = playIndex
    }


    override fun onDestroy() {
        super.onDestroy()
        playModelList.clear()
        playModelList.clear()
        timerTask.cancel()
        timer.cancel()
        abandonAudioFocus()
    }

    final override fun addPlayerListener(listener: SimplePlayerListener) {
        if (!playerListener.contains(listener)) {
            playerListener.add(listener)
        }
    }

    final override fun removePlayerListener(listener: SimplePlayerListener) {
        playerListener.remove(listener)
    }

    final override fun getCurrentModel(): PlayerModel? = currentPlayModel

    final override fun getAllPlayModel(): MutableList<PlayerModel> {
        return playModelList
    }

    final override fun getCurrentPlayIndex(): Int = currentIndex

    final override fun getRepeatMode(): Int {
        return currentLoopMode
    }

    final override fun setRepeatMode(mode: Int) {
        currentLoopMode = mode
    }

    /**
     * 回调播放器
     */
    fun doPlayerListener(model: Function1<SimplePlayerListener, Unit>) {
        playerListener.forEach {
            model.invoke(it)
        }
    }

    final override fun setAudioForceEnable(enable: Boolean) {
        if (enable) {
            if (audioFocusHelper == null) {
                audioFocusHelper = AudioFocusHelper(context, getAudioForceListener())
            }
        } else {
            audioFocusHelper = null
        }
    }

    /**
     * 请求焦点
     * 返回值是否启用焦点处理
     */
    internal fun requestAudioFocus(): Boolean {
        audioFocusHelper?.requestAudioFocus()
        return audioFocusHelper != null
    }

    /**
     * 中断焦点
     * 返回值是否启用焦点处理
     */
    private fun abandonAudioFocus(): Boolean {
        audioFocusHelper?.abandonAudioFocus()
        return audioFocusHelper != null
    }

    /**
     * 获取音频焦点监听
     */
    internal abstract fun getAudioForceListener(): AudioManager.OnAudioFocusChangeListener

    /**
     * 如果添加生命周期
     * 当不可见的时候，暂停播放
     */
    override fun onStop() {
        super.onStop()
        pause()
    }

    override fun pause(listener: ((PlayerModel?) -> Unit)?) {
        abandonAudioFocus()
    }

    override fun stop(listener: ((PlayerModel?) -> Unit)?) {
        abandonAudioFocus()
    }
}