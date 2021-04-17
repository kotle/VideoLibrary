package com.yizisu.playerlibrary.helper

import android.net.Uri
import com.google.android.exoplayer2.MediaItem
import com.yizisu.playerlibrary.impl.exoplayer.getCountTimeByLong
import kotlin.math.max
import kotlin.math.min

/**
 * 一个播放对象，所以播放资源通过这个对象传递
 */
@Deprecated("使用[IYzsPlayer.Model]替代")
abstract class PlayerModel {
    //文件类型,rtmp,http等，用于创建mediaSource是用
    var overrideExtension: String? = null

    //视频宽高
    internal var _videoWidth: Int = 0
    internal var _videoHeight: Int = 0

    //当前的资源item
    internal var _mediaItem: MediaItem? = null
    val mediaItem: MediaItem?
        get() = _mediaItem

    //视频总时间
    internal var _totalDuration: Long = 0
    val totalDuration: Long
        get() = max(_totalDuration, 0L)
    val totalDurationText: String
        get() = getCountTimeByLong(_totalDuration)

    //当前播放时间
    internal var _currentDuration: Long = 0
    val currentDuration: Long
        get() = max(min(_currentDuration, totalDuration), 0)
    val currentDurationText: String
        get() = getCountTimeByLong(_currentDuration)

    //当前已经缓存的时间
    internal var _currentBufferDuration: Long = 0
    val currentBufferDuration: Long
        get() = max(0, min(_currentBufferDuration, totalDuration))
    val currentBufferDurationText: String
        get() = getCountTimeByLong(_currentBufferDuration)

    val videoWidth: Int
        get() = _videoWidth
    val videoHeight: Int
        get() = _videoHeight

    /**
     * 回调播放链接地址
     * 支持子线程回调
     * 回调完毕才会播放
     *  如果有错误，请给Throwable赋值，并且Uri赋值为null
     *  Boolean是否需要再次调用onPlayChange
     *  如果播放的model被换掉，会回调onPlayModelNotThis()
     */
    @Deprecated("[callMediaItem]代替")
    open fun callMediaUri(
        uriCall: (
            Uri?/*播放链接*/, Throwable?/*无法传入播放链接的时候错误回调*/,
            Boolean/*是否需要再回调一次PlayModelChange*/
        ) -> Unit
    ) {

    }

    /**
     * 回调播放链接地址
     * 支持子线程回调
     * 回调完毕才会播放
     *  如果有错误，请给Throwable赋值，并且Uri赋值为null
     *  Boolean是否需要再次调用onPlayChange
     *  如果播放的model被换掉，会回调onPlayModelNotThis()
     */
    open fun callMediaItem(
        uriCall: (
            MediaItem?/*播放链接*/,
            Throwable?/*无法传入播放链接的时候错误回调*/,
            Boolean/*是否需要再回调一次PlayModelChange*/
        ) -> Unit
    ) {
        callMediaUri { uri, throwable, b ->
            if (uri != null) {
                uriCall.invoke(MediaItem.fromUri(uri), throwable, b)
            } else {
                uriCall.invoke(null, throwable, b)
            }
        }
    }

    /**
     * 但播放链接出现错误，再次回调这个，根据情况自行处理
     * 注意： 当这里播放链接也发生错误，容易发生死循环
     * 播放器错误--回调函数---播放器错误---回调函数
     */
    open fun callMediaItemWhenError(
        error: Throwable,
        uriCall: (MediaItem?/*播放链接*/, Throwable?/*无法传入播放链接的时候错误回调*/, Boolean/*是否需要再回调一次PlayModelChange*/) -> Unit
    ) {

    }

    /**
     * 准备url的时候出现错误
     */
    open fun onError(error: Throwable) {

    }

    /**
     * 整个播放器被销毁的时候回调
     */
    open fun onDestroy() {

    }

    /**
     * 播放资源被切换的时候调用
     */
    open fun onPlayModelNotThis() {

    }

    /**
     * 播放资源被切换的时候调用
     * 自身就是之前的
     * newModel为新的资源
     */
    open fun onPlayModelNotThis(newModel: PlayerModel?) {

    }

    /**
     * 获取标题
     */
    open fun getTitle(): CharSequence? {
        return null
    }
}