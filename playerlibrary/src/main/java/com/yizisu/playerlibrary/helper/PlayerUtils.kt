package com.yizisu.playerlibrary.helper

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.yizisu.playerlibrary.BuildConfig

/**
 * 打印日志
 */
internal fun Any.logI(message: String?) {
    if (BuildConfig.DEBUG) {
        Log.i(javaClass.simpleName, message ?: "null")
    }
}

