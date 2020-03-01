package com.yizisu.playerlibrary

import android.view.TextureView
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener

/**
 * 播放器操作类
 */
internal interface IYzsPlayer : PlayerLifecycleObserver {

    /**
     * 开始播放
     * 返回值，是否成功播放
     */
    fun play(listener: Function1<PlayerModel?, Unit>? = null)

    /**
     * 准备资源
     * 需要手动再调用播放
     */
    fun prepare(models: MutableList<PlayerModel>, listener: Function1<PlayerModel?, Unit>? = null)

    /**
     * 准备完毕就播放
     */
    fun prepareAndPlay(
        models: MutableList<PlayerModel>,
        listener: Function1<PlayerModel?, Unit>? = null
    )

    /**
     * 暂停播放
     * 返回值，是否成功暂停
     */
    fun pause(listener: Function1<PlayerModel?, Unit>? = null)


    /**
     * 停止播放
     * reset：是否清空资源
     */
    fun stop(listener: Function1<PlayerModel?, Unit>? = null)

    /**
     * 下一个
     */
    fun next(listener: Function1<PlayerModel?, Unit>? = null)

    /**
     * 上一个
     */
    fun previous(listener: Function1<PlayerModel?, Unit>? = null)

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
        listener: Function1<PlayerModel?, Unit>? = null
    )

    /**
     * 添加监听
     */
    fun addPlayerListener(listener: SimplePlayerListener)

    /**
     * 移除监听
     */
    fun removePlayerListener(listener: SimplePlayerListener)

}