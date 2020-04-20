package com.yizisu.playerlibrary


import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.TextureView
import androidx.annotation.IntDef
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener

/**
 * 播放器操作类
 */
interface IYzsPlayer<Model : PlayerModel> : PlayerLifecycleObserver {
    @MustBeDocumented
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        PlayerFactory.LOOP_MODO_LIST,
        PlayerFactory.LOOP_MODO_NONE,
        PlayerFactory.LOOP_MODO_SHUFF,
        PlayerFactory.LOOP_MODO_SINGLE
    )
    annotation class SimplePlayerRepeatMode

    /**
     * 开始播放
     */
    fun play(listener: Function1<Model?, Unit>? = null)

    /**
     * 准备资源
     * 需要手动再调用播放
     */
    fun prepare(
        models: MutableList<Model>,
        playIndex: Int = 0,
        isStopLastMedia: Boolean = true,
        listener: Function1<Model?, Unit>? = null
    )

    /**
     * 准备完毕就播放
     */
    fun prepareAndPlay(
        models: MutableList<Model>,
        playIndex: Int = 0,
        isStopLastMedia: Boolean = true,
        listener: Function1<Model?, Unit>? = null
    )

    /**
     * 暂停播放
     */
    fun pause(listener: Function1<Model?, Unit>? = null)

    /**
     * 停止播放
     */
    fun stop(listener: Function1<Model?, Unit>? = null)

    /**
     * 下一个
     */
    fun next(listener: Function1<Model?, Unit>? = null)

    /**
     * 上一个
     */
    fun previous(listener: Function1<Model?, Unit>? = null)

    /**
     * 设置一个界面
     */
    fun attachView(view: TextureView)

    /**
     * 跳转
     */
    fun seekTo(
        positionMs: Long,
        index: Int? = null,
        listener: Function1<Model?, Unit>? = null
    )

    /**
     * 跳转
     */
    fun seekRatioTo(
        ratio: Float,
        listener: Function1<Model?, Unit>? = null
    )

    /**
     * 添加监听
     */
    fun addPlayerListener(listener: SimplePlayerListener<Model>)

    /**
     * 移除监听
     */
    fun removePlayerListener(listener: SimplePlayerListener<Model>)

    /**
     * 当前播放的model对象
     */
    fun getCurrentModel(): Model?

    /**
     * 获取播放列表
     */
    fun getAllPlayModel(): MutableList<Model>

    /**
     * 当前播放索引
     */
    fun getCurrentPlayIndex(): Int

    /**
     * 获取当前播放状态
     */
    fun isPlaying(): Boolean

    /**
     * 安卓媒体框架
     */
    fun setMediaSession(
        session: MediaSessionCompat,
        bundleCall: (Model?) -> MediaDescriptionCompat
    )

    /**
     * 循环模式
     *
     */
    fun setRepeatMode(@SimplePlayerRepeatMode mode: Int)

    /**
     * 循环模式
     * SimplePlayer.LOOP_MODO_NONE
     *
     */
    @SimplePlayerRepeatMode
    fun getRepeatMode(): Int

    /**
     * 是否设置锁屏任然唤醒cup
     */
    fun setHandleWakeLock(handleWakeLock: Boolean)

    /**
     * 设置音量
     *  0.0-1.0
     */
    fun setVolume(volume: Float)

    /**
     * 获取音量
     */
    fun getVolume(volume: Float): Float

    /**
     * 是否启用音频焦点管理
     */
    fun setAudioForceEnable(enable: Boolean)

    /**
     * 设置播放速度
     */
    fun setVideoSpeed(speed: Float)
}