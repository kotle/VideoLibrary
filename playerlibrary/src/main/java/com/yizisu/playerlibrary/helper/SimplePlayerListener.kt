package com.yizisu.playerlibrary.helper

import android.media.AudioManager

interface SimplePlayerListener : AudioManager.OnAudioFocusChangeListener {
    /**
     * 每秒钟回调一次
     */
    fun onTick(playerModel: PlayerModel)

    /**
     * 当视频发生错误
     */
    fun onError(throwable: Throwable, playerModel: PlayerModel?) {}

    /**
     * 当视频播放
     */
    fun onPlay(playStatus: Boolean, playerModel: PlayerModel?) {}

    /**
     * 可能由于
     * 1.缓存暂停
     * 2.手动暂停
     * playStatus：当前播放状态 true 播放 false 暂停
     */
    fun onPause(playStatus: Boolean, playerModel: PlayerModel?) {}

    /**
     * 停止播放
     * 播放器调用停止播放的时候回调
     */
    fun onStop(playStatus: Boolean, playerModel: PlayerModel?) {}

    /**
     * 当缓存状态发生变化
     */
    fun onBufferStateChange(isBuffering: Boolean, playStatus: Boolean, playerModel: PlayerModel?) {}

    /**
     * 当播放发生变化
     */
    fun onPlayerModelChange(playerModel: PlayerModel) {}

    /**
     * 获取视频尺寸
     */
    fun onVideoSizeChange(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float,
        playerModel: PlayerModel?
    ) {

    }

    /**
     * 视频焦点回调
     * 需要先启用
     * 调用IYzsPlayer.setAudioForceEnable(true)
     */
    override fun onAudioFocusChange(focusChange: Int) {

    }
}