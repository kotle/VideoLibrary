package com.yizisu.playerlibrary.helper

import android.app.Activity
import android.media.AudioManager
import android.os.Build
import android.view.*
import kotlin.math.max

/**
 * 调节屏幕亮度（0-1.0）
 * percent：亮度新增比例
 * return 当前亮度
 */
internal fun View.setScreenBrightnessSlide(percent: Float): Float {
    (context as? Activity)?.apply {
        return this.setScreenBrightnessSlide(percent)
    }
    return 0f
}

fun Activity.getCurrentBrightnessSlide(): Float {
    return window.attributes.screenBrightness
}

fun View.getCurrentBrightnessSlide(): Float {
    (context as? Activity)?.apply {
        return this.getCurrentBrightnessSlide()
    }
    return 0f
}

fun Activity.setScreenBrightnessSlide(percent: Float): Float {
    var mBrightnessData = getCurrentBrightnessSlide()
    if (mBrightnessData <= 0.00f) {
        mBrightnessData = 0.50f
    } else if (mBrightnessData < 0.01f) {
        mBrightnessData = 0.01f
    }
    val lpa = window.attributes
    lpa.screenBrightness = mBrightnessData + percent
    if (lpa.screenBrightness > 1.0f) {
        lpa.screenBrightness = 1.0f
    } else if (lpa.screenBrightness < 0.01f) {
        lpa.screenBrightness = 0.01f
    }
    window.attributes = lpa
    return lpa.screenBrightness
}

fun Window?.fullScreen(isFullScreen: Boolean) {
    val window = this ?: return
    val lp = window.attributes
    if (isFullScreen) {
        // 延伸显示区域到耳朵区
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.windowInsetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsets.Type.statusBars())
                hide(WindowInsets.Type.navigationBars())
                setDecorFitsSystemWindows(false)
            }
        } else {
            // 允许内容绘制到耳朵区
            val decorView = window.decorView
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if (decorView.tag == null) {
                decorView.tag = decorView.systemUiVisibility
            }
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            window.attributes = lp
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.windowInsetsController?.apply {
                show(WindowInsets.Type.statusBars())
                show(WindowInsets.Type.navigationBars())
                setDecorFitsSystemWindows(true)
            }
        } else {
            clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if (decorView.tag is Int){
                decorView.systemUiVisibility = decorView.tag as Int
            }
        }
    }
}

fun Activity?.fullScreen(isFullScreen: Boolean) {
    this?.window?.fullScreen(isFullScreen)
}

private var offVolume = 0f
internal fun View.clearAdjustVolume() {
    offVolume = 0f
}

internal fun View.adjustVolume(audioManager: AudioManager, offY: Float): Int {
    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    offVolume += offY * max / height
    if (offVolume < 1 && offVolume > -1) {
        return currentVolume
    }
    val offVolumeInt = offVolume.toInt()
    offVolume -= offVolumeInt
    val setVolume = currentVolume + offVolumeInt
    val volume = when {
        setVolume >= max -> {
            max
        }
        setVolume <= 0f -> {
            0
        }
        else -> {
            setVolume
        }
    }
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    return volume
}

internal fun View.setVolume(audioManager: AudioManager, offY: Float): Float {
    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    offVolume += offY * max / height
    if (offVolume < 1 && offVolume > -1) {
        return currentVolume.toFloat() / max
    }
    val offVolumeInt = offVolume.toInt()
    offVolume -= offVolumeInt
    val setVolume = currentVolume + offVolumeInt
    val volume = when {
        setVolume >= max -> {
            max
        }
        setVolume <= 0f -> {
            0
        }
        else -> {
            setVolume
        }
    }
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    return volume.toFloat() / max
}

//fun View.setDisplayInNotch() {
//    setDisplayInNotch(context as Activity)
//}

/**
 * 设置全屏
 */
/*fun setDisplayInNotch(activity: Activity) {
    val window = activity.window
    // 延伸显示区域到耳朵区
    val lp = window.attributes
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        lp.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
    window.attributes = lp
    // 允许内容绘制到耳朵区
    val decorView = window.decorView
    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
}*/

/**
 * 毫秒换成00:00:00
 */
internal fun getCountTimeByLong(time: Long): String {
    val finishTime = max(time, 0L)
    var totalTime = (finishTime / 1000).toInt()//秒
    var hour = 0
    var minute = 0
    var second = 0

    if (3600 <= totalTime) {
        hour = totalTime / 3600
        totalTime -= 3600 * hour
    }
    return getCountTimeByLong(finishTime, hour > 0)
}

/**
 * 毫秒换成00:00:00
 */
internal fun getCountTimeByLong(finishTime: Long, isNeedHour: Boolean): String {
    if (finishTime <= 0) {
        return if (isNeedHour) {
            "00:00:00"
        } else {
            "00:00"
        }
    }
    var totalTime = (finishTime / 1000).toInt()//秒
    var hour = 0
    var minute = 0
    var second = 0

    if (3600 <= totalTime) {
        hour = totalTime / 3600
        totalTime -= 3600 * hour
    }
    if (60 <= totalTime) {
        minute = totalTime / 60
        totalTime -= 60 * minute
    }
    if (0 <= totalTime) {
        second = totalTime
    }
    val sb = StringBuilder()
    if (isNeedHour) {
        if (hour < 10) {
            sb.append("0").append(hour).append(":")
        } else {
            sb.append(hour).append(":")
        }
    }
    if (minute < 10) {
        sb.append("0").append(minute).append(":")
    } else {
        sb.append(minute).append(":")
    }
    if (second < 10) {
        sb.append("0").append(second)
    } else {
        sb.append(second)
    }
    return sb.toString()
}