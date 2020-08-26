package com.yizisu.playerlibrary.helper

import android.net.Uri
import com.yizisu.playerlibrary.impl.exoplayer.getCountTimeByLong

/**
 * 一个播放对象，所以播放资源通过这个对象传递
 */
abstract class PlayerModel {
    //文件类型,rtmp,http等，用于创建mediaSource是用
    var overrideExtension: String? = null

    //视频总时间
    internal var _videoWidth: Int = 0
    internal var _videoHeight: Int = 0
    internal var _totalDuration: Long = 0
    val totalDuration: Long
        get() = _totalDuration
    val totalDurationText: String
        get() = getCountTimeByLong(_totalDuration)

    //当前播放时间
    internal var _currentDuration: Long = 0
    val currentDuration: Long
        get() = _currentDuration
    val currentDurationText: String
        get() = getCountTimeByLong(_currentDuration)

    //当前已经缓存的时间
    internal var _currentBufferDuration: Long = 0
    val currentBufferDuration: Long
        get() = _currentBufferDuration
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
     */
    abstract fun callMediaUri(
        uriCall: (
            Uri?/*播放链接*/, Throwable?/*无法传入播放链接的时候错误回调*/,
            Boolean/*是否需要再回调一次PlayModelChange*/
        ) -> Unit
    )

    open fun onError(error: Throwable) {

    }
}