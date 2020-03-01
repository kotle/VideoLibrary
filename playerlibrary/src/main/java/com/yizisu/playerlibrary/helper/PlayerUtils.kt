package com.yizisu.playerlibrary.helper

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.yizisu.playerlibrary.BuildConfig
import com.yizisu.playerlibrary.SimplePlayer

/**
 * 打印日志
 */
internal fun Any.logI(message: String?) {
    if (BuildConfig.DEBUG) {
        Log.i(javaClass.simpleName, message ?: "null")
    }
}

/**
 * 创建带有生命周期的SimplePlayer
 */
fun createLifecycleSimplePlayer(lifecycle: AppCompatActivity): SimplePlayer {
    val player = SimplePlayer(lifecycle)
    lifecycle.lifecycle.addObserver(player)
    return player
}

/**
 * 创建带有生命周期的SimplePlayer
 */
fun createLifecycleSimplePlayer(lifecycle: Fragment): SimplePlayer {
    val player = SimplePlayer(lifecycle.context!!)
    lifecycle.lifecycle.addObserver(player)
    return player
}