package com.yizisu.playerlibrary

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.yizisu.playerlibrary.impl.BaseYzsPlayer
import com.yizisu.playerlibrary.impl.exoplayer.ExoPlayerImpl

/**
 * 一个简单的播放器
 */
class SimplePlayer(
    private val context: Context,
    private val playerImpl: BaseYzsPlayer = ExoPlayerImpl(context)
) : IYzsPlayer by playerImpl{
    companion object{
        //单个循环
        const val LOOP_MODO_SINGLE=1
        //全部循环
        const val LOOP_MODO_LIST=2
        //不循环
        const val LOOP_MODO_NONE=3
        //随机下一个
        const val LOOP_MODO_SHUFF=4
    }
}
