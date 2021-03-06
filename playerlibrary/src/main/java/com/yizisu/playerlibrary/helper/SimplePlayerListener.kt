package com.yizisu.playerlibrary.helper

import android.media.AudioManager
import com.yizisu.playerlibrary.IYzsPlayer

interface SimplePlayerListener<Model : PlayerModel> : AudioManager.OnAudioFocusChangeListener {
    /**
     * 每秒钟回调一次
     */
    fun onTick(playerModel: Model) {

    }

    /**
     * 每秒钟回调一次,只是在子线程回调
     */
    fun onTickInWorkThread(playerModel: Model) {

    }

    /**
     * 当视频发生错误
     */
    fun onError(throwable: Throwable, playerModel: Model?) {}

    /**
     * 开始准备资源
     */
    fun onPrepare(playerModel: Model?) {}

    /**
     * 当视频播放
     */
    fun onPlay(playStatus: Boolean, playerModel: Model?) {}

    /**
     * 可能由于
     * 1.缓存暂停
     * 2.手动暂停
     * playStatus：当前播放状态 true 播放 false 暂停
     */
    fun onPause(playStatus: Boolean, playerModel: Model?) {}

    /**
     * 停止播放
     * 播放器调用停止播放的时候回调
     */
    fun onStop(playStatus: Boolean, playerModel: Model?) {}


    /**
     * 当缓存状态发生变化
     */
    fun onBufferStateChange(isBuffering: Boolean, playStatus: Boolean, playerModel: Model?) {}

    /**
     * 当播放发生变化，有可能上一次和这一次的model是同一个对象
     */
    fun onPlayerModelChange(playerModel: Model) {}

    /**
     * 当播放发生变化,并且上一次和这次的model不一样
     */
    fun onPlayerModelChange(lastModel: Model?, playerModel: Model) {}

    /**
     * 当播放列表发生变化
     */
    fun onPlayerListChange(playerModels: MutableList<Model>) {}

    /**
     * 当渲染第一帧
     */
    fun onRenderedFirstFrame(playerModel: Model?) {

    }

    /**
     * 获取视频尺寸
     */
    fun onVideoSizeChange(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float,
        playerModel: Model?
    ) {

    }

    /**
     * 视频焦点回调
     * 需要先启用
     * 调用IYzsPlayer.setAudioForceEnable(true)
     */
    override fun onAudioFocusChange(focusChange: Int) {

    }

    /**
     * 当监听器被添加的时候 回调
     */
    fun onListenerAdd(playerModel: Model?) {

    }

    /**
     * 当监听器移除的时候 回调
     */
    fun onListenerRemove(playerModel: Model?) {

    }

    /**
     * 当播放器销毁监听
     */
    fun onPlayerDestroy(player: IYzsPlayer<Model>, playerModel: Model?) {

    }
}