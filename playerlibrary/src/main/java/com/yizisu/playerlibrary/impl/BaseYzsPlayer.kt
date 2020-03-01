package com.yizisu.playerlibrary.impl

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.SimplePlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import java.util.*

abstract class BaseYzsPlayer : IYzsPlayer {
    //当前播放模式，支持四种
    private var currentLoopMode = SimplePlayer.LOOP_MODO_NONE
    //这个索引，不经过各种判断直接赋值
    protected var _currentIndex=0
    //设置当前索引
    open var currentIndex
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
    protected val playerListener = mutableListOf<SimplePlayerListener>()
    private val mainHandler = Handler(Looper.getMainLooper())
    //定时任务
    private val timerTask = object : TimerTask() {
        override fun run() {
            if (playModelList.isNotEmpty()) {
                mainHandler.post {
                    currentPlayModel?.let { model ->
                        playerListener.forEach {
                            model._totalDuration = totalDuration
                            model._currentDuration = currentDuration
                            model._currentBufferDuration = currentBufferDuration
                            model._bufferedPercentage = bufferedPercentage
                            it.onBufferChange(model)
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
    //缓存的百分比
    open val bufferedPercentage: Int = 0

    override fun prepare(models: MutableList<PlayerModel>, listener: ((PlayerModel?) -> Unit)?) {
        playModelList.clear()
        playModelList.addAll(models)
        currentIndex = 0
    }


    override fun onDestroy() {
        super.onDestroy()
        playModelList.clear()
        timerTask.cancel()
        timer.cancel()
    }

    final override fun addPlayerListener(listener: SimplePlayerListener) {
        if (!playerListener.contains(listener)) {
            playerListener.add(listener)
        }
    }

    final override fun removePlayerListener(listener: SimplePlayerListener) {
        playerListener.remove(listener)
    }
}