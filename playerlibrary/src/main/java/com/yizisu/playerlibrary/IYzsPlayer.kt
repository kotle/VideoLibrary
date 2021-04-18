package com.yizisu.playerlibrary


import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.impl.exoplayer.ExoPlayerImpl
import com.yizisu.playerlibrary.view.SimplePlayerRepeatMode

/**
 * 播放器操作类
 */
interface IYzsPlayer<Model : PlayerModel> : PlayerLifecycleObserver {


    companion object {
        /**
         * 创建播放器
         */
        operator fun <Model : PlayerModel> invoke(context: Context): IYzsPlayer<Model> {
            return ExoPlayerImpl(context.applicationContext)
        }

        operator fun <Model : PlayerModel> invoke(activity: AppCompatActivity, enableLifecycle: Boolean = true): IYzsPlayer<Model> {
            return ExoPlayerImpl<Model>(activity.applicationContext).apply {
                if (enableLifecycle) {
                    activity.lifecycle.addObserver(this)
                }
            }
        }

        operator fun <Model : PlayerModel> invoke(fragment: Fragment, enableLifecycle: Boolean = true): IYzsPlayer<Model> {
            return ExoPlayerImpl<Model>(fragment.context!!.applicationContext).apply {
                if (enableLifecycle) {
                    fragment.lifecycle.addObserver(this)
                }
            }
        }
    }

    /**
     * 保存播放的状态信息
     */
    data class Info<T : PlayerModel>(
            var playModes: MutableList<T>,
            var currentIndex: Int
    )

    /**
     * 针对过时函数更新
     */
    abstract class Model : PlayerModel() {
        final override fun callMediaItem(uriCall: (MediaItem?, Throwable?, Boolean) -> Unit) {
            onMediaItem(uriCall)
        }

        final override fun callMediaUri(uriCall: (Uri?, Throwable?, Boolean) -> Unit) {
            super.callMediaUri(uriCall)
        }

        abstract fun onMediaItem(uriCall: (MediaItem?, Throwable?, Boolean) -> Unit)
    }


    //视频总时间
    val totalDuration: Long

    //当前播放时间
    val currentDuration: Long

    //当前已经缓存的时间
    val currentBufferDuration: Long

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
            playIndex: Int = -1,
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
     * 是否可以播发上一个
     */
    fun canPlayNext(): Boolean

    /**
     * 是否可以播放上一个
     */
    fun canPlayPrevious(): Boolean

    /**
     * 上一个
     */
    fun previous(listener: Function1<Model?, Unit>? = null)

    /**
     * 设置一个界面
     */
    fun attachView(view: TextureView?)
    fun clearView(view: TextureView?)

    /**
     * 跳转
     */
    fun seekTo(
            positionMs: Long,
            index: Int? = null,
            reset: Boolean = false,
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
     * 添加列表
     */
    fun addPlayModels(modes: MutableList<Model>)

    /**
     * 添加列表
     */
    fun addPlayModels(index: Int, modes: MutableList<Model>)

    /**
     * 添加列表
     */
    fun addPlayModel(mode: Model)

    /**
     * 移除
     */
    fun removePlayModel(mode: Model)

    /**
     * remove
     */
    fun removePlayModel(index: Int)

    /**
     * 添加列表
     */
    fun addPlayModel(index: Int, mode: Model)

    /**
     * 替换列表，会改变原有的对象引用
     */
    fun setPlayerInfo(newInfo: Info<Model>)

    /**
     * 当前播放索引
     */
    fun getCurrentPlayIndex(): Int

    /**
     * 获取当前播放状态
     */
    fun isPlaying(): Boolean

    /**
     * 当前是否正在缓冲
     * ignoredPlayStatus:true 不管是否处于播放状态，只要缓冲就返回true
     * false:只有播放状态并且缓冲才返回true
     */
    fun isBuffering(ignoredPlayStatus: Boolean): Boolean

    /**
     * 安卓媒体框架
     */
    fun setMediaSession(
            session: MediaSessionCompat,
            bundleCall: (Model?) -> MediaDescriptionCompat
    )

    /**
     * 设置列表循环模式
     *
     */
    fun setRepeatMode(@SimplePlayerRepeatMode mode: Int)

    /**
     * 设置单个model播放模式
     */
    fun setSingleModelRepeatModel(@PlayerFactory.RepeatMode repeatMode: Int)

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
    @Deprecated("setWakeMode")
    fun setHandleWakeLock(handleWakeLock: Boolean)

    /**
     * 设置锁屏模式
     */
    fun setWakeMode(@C.WakeMode wakeMode: Int)

    /**
     * 设置音量
     *  0.0-1.0
     */
    fun setVolume(volume: Float)

    /**
     * 获取音量
     */
    fun getVolume(): Float

    /**
     * 是否启用音频焦点管理
     */
    fun setAudioForceEnable(enable: Boolean)

    /**
     * 设置播放速度
     */
    fun setVideoSpeed(speed: Float)

    /**
     * 获取播放速度
     */
    fun getVideoSpeed(): Float

    /**
     * 重试
     * isKeepProgress 是否保持当前进度
     */
    fun retry(isKeepProgress: Boolean = true, listener: ((Model?) -> Unit)? = null)
}