package com.yizisu.playerlibrary.impl

import android.content.Context
import android.media.AudioManager
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.PlayerFactory
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.impl.exoplayer.mainHandler
import java.lang.ref.WeakReference
import java.util.*

internal abstract class BaseYzsPlayer<Model : PlayerModel>(internal val contextWrf: WeakReference<Context?>?) :
    IYzsPlayer<Model> {
    internal val context: Context?
        get() = contextWrf?.get()

    //播放倍速
    private var _speed = 1f

    //当前播放模式，支持四种
    private var currentLoopMode = PlayerFactory.LOOP_MODO_NONE

//    //这个索引，不经过各种判断直接赋值
//    protected var _currentIndex = 0

    //设置当前索引
    var currentIndex
        get() = info.currentIndex
        set(value) {
            val count = playModelList.count()
            info.currentIndex = if (count == 0) {
                0
            } else {
                when (currentLoopMode) {
                    PlayerFactory.LOOP_MODO_SINGLE -> {
                        info.currentIndex
                    }
                    PlayerFactory.LOOP_MODO_LIST -> {
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
                    PlayerFactory.LOOP_MODO_SHUFF -> {
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
    private val playerListener = mutableListOf<SimplePlayerListener<Model>>()

    //定时任务
    protected val timerTask = object : TimerTask() {
        override fun run() {
            if (playModelList.isNotEmpty() && context != null) {
                mainHandler.post(tickRunnable)
            }
        }
    }

    private val tickRunnable = Runnable {
        currentPlayModel?.let { model ->
            doPlayerListener {
                model._totalDuration = totalDuration
                model._currentDuration = currentDuration
                model._currentBufferDuration = currentBufferDuration
                it.onTick(model)
            }
        }
    }

    /**
     * 设置为守护线程
     * 其他线程销毁的时候，自动结束此线程
     */
    private val timer = Timer(true)

    protected var info = IYzsPlayer.Info<Model>(mutableListOf(), 0)

    //是否支持处理音频焦点
    //若处理，则有焦点才播放，失去焦点停止播放
    private var audioFocusHelper: AudioFocusHelper? = null

    //当前播放列表集合
    protected val playModelList: MutableList<Model>
        get() = info.playModes

    //当前播放的model
    val currentPlayModel: Model?
        get() {
            return if (currentIndex < playModelList.count() && currentIndex >= 0) {
                playModelList[currentIndex]
            } else {
                null
            }
        }


    init {
        timer.schedule(timerTask, 0, 1000)
    }

    override fun prepare(
        models: MutableList<Model>,
        playIndex: Int,
        isStopLastMedia: Boolean,
        listener: ((Model?) -> Unit)?
    ) {
        playModelList.clear()
        playModelList.addAll(models)
        info.currentIndex = playIndex
        doPlayerListener {
            it.onPlayerListChange(playModelList)
        }
    }

    override fun setVideoSpeed(speed: Float) {
        _speed = speed
    }

    override fun getVideoSpeed(): Float {
        return _speed
    }

    override fun onDestroy() {
        super.onDestroy()
        timerTask.cancel()
        timer.cancel()
        doPlayerListener {
            it.onListenerRemove(currentPlayModel)
            //回调播放器销魂
            it.onPlayerDestroy(this, currentPlayModel)
        }
        playModelList.forEach {
            it.onDestroy()
        }
        playModelList.clear()
        playerListener.clear()
        abandonAudioFocus()
        contextWrf?.clear()

    }

    final override fun addPlayerListener(listener: SimplePlayerListener<Model>) {
        if (!playerListener.contains(listener)) {
            playerListener.add(listener)
            listener.onListenerAdd(currentPlayModel)
        }
    }

    final override fun removePlayerListener(listener: SimplePlayerListener<Model>) {
        if (playerListener.contains(listener)) {
            playerListener.remove(listener)
            listener.onListenerRemove(currentPlayModel)
        }
    }

    final override fun getCurrentModel(): Model? = currentPlayModel

    final override fun getAllPlayModel(): MutableList<Model> {
        return playModelList
    }

    final override fun getCurrentPlayIndex(): Int = currentIndex

    final override fun getRepeatMode(): Int {
        return currentLoopMode
    }

    final override fun setRepeatMode(mode: Int) {
        currentLoopMode = mode
        if (mode == PlayerFactory.LOOP_MODO_SINGLE) {
            setSingleModelRepeatModel(PlayerFactory.REPEAT_MODE_ALL)
        } else {
            setSingleModelRepeatModel(PlayerFactory.REPEAT_MODE_OFF)
        }
    }

    /**
     * 回调播放器
     */
    @Synchronized
    fun doPlayerListener(model: Function1<SimplePlayerListener<Model>, Unit>) {
        //用新的集合，防止循环的时候移除和添加监听导致异常
        mutableListOf<SimplePlayerListener<Model>>().apply { addAll(playerListener) }.forEach {
            model.invoke(it)
        }
    }

    final override fun setAudioForceEnable(enable: Boolean) {
        if (enable) {
            if (audioFocusHelper == null) {
                context?.let {
                    audioFocusHelper = AudioFocusHelper(it, getAudioForceListener())
                }
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

    override fun play(listener: ((Model?) -> Unit)?) {
        doPlayerListener {
            it.onPlay(true, currentPlayModel)
        }
    }

    override fun pause(listener: ((Model?) -> Unit)?) {
        abandonAudioFocus()
        doPlayerListener {
            it.onPause(false, currentPlayModel)
        }
    }

    override fun stop(listener: ((Model?) -> Unit)?) {
        abandonAudioFocus()
    }

    override fun addPlayModel(mode: Model) {
        addPlayModel(0, mode)
    }

    override fun addPlayModel(index: Int, mode: Model) {
        if (index <= getCurrentPlayIndex()) {
            currentIndex++
        }
        getAllPlayModel().add(index, mode)
        doPlayerListener {
            it.onPlayerListChange(getAllPlayModel())
        }
    }

    override fun setPlayerInfo(newInfo: IYzsPlayer.Info<Model>) {
        info = newInfo
        doPlayerListener {
            it.onPlayerListChange(getAllPlayModel())
        }
    }

    override fun removePlayModel(index: Int) {
        val list = getAllPlayModel()
        if (index >= 0 && index < list.count()) {
            val model = list[index]
            removePlayModel(model)
        }
    }

    override fun removePlayModel(mode: Model) {
        val current = currentPlayModel
        val isPlaying = isPlaying()
        val list = getAllPlayModel()
        val index = list.indexOf(mode)
        if (index < getCurrentPlayIndex() && index >= 0) {
            currentIndex--
        }
        list.remove(mode)
        doPlayerListener {
            it.onPlayerListChange(list)
        }
        if (mode == current) {
            seekTo(0, index, true)
            if (isPlaying) {
                play()
            } else {
                pause()
            }
        }
    }

    override fun addPlayModels(modes: MutableList<Model>) {
        addPlayModels(0, modes)
    }

    override fun addPlayModels(index: Int, modes: MutableList<Model>) {
        if (index <= getCurrentPlayIndex()) {
            currentIndex += modes.count()
        }
        getAllPlayModel().addAll(index, modes)
        doPlayerListener {
            it.onPlayerListChange(getAllPlayModel())
        }
    }
}