package com.yizisu.playerlibrary.helper

import android.net.Uri
import com.yizisu.playerlibrary.impl.exoplayer.getCountTimeByLong

/**
 * 一个播放对象，所以播放资源通过这个对象传递
 */
abstract class PlayerModel {
    //文件类型
    var overrideExtension: String? = null
    //视频总时间
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

    //播放链接
    abstract fun getMediaUri(): Uri
}