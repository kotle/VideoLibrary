package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.media.AudioManager
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.setScreenBrightnessSlide
import com.yizisu.playerlibrary.helper.setVolume
import kotlin.math.abs

/**
 * 滑动屏幕，显示中间提示框
 */
internal class SwipeViewHelper(
    private val gestureView: VideoPlayerGestureView,
    private val hint: MidProgressHintView
) {
    //记录此次滑动屏幕的方向
    private var scrollOrientation: Int? = null

    //整个布局的宽度
    private val width: Int
        get() = gestureView.width

    //整个布局的高度
    private val height: Int
        get() = gestureView.height

    //音频管理器，调节音量
    private val mAudioManager by lazy { gestureView.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    //一次完整事件滑动的总距离
    private var swipeY = 0f

    //一次完整事件滑动的总距离
    private var swipeX = 0f

    //需要调节到的视频进度
    private var progress: Long? = null

    //手指按下的时候记录当前model
    private var currentModel: PlayerModel? = null

    //手指按下的时候记录当前model
    private var lastModelProgress: Long? = null

    init {
        gestureView.gestureDetectorHelper.onActionDownListener = {
            scrollOrientation = null
            progress = null
            currentModel = gestureView.player?.getCurrentModel()
            lastModelProgress = currentModel?.currentDuration
            swipeX = 0f
            swipeY = 0f
        }

        gestureView.gestureDetectorHelper.setOnScrollListener { e1, e2, x, y ->
            swipeX += x
            swipeY += y
            when (scrollOrientation) {
                LinearLayout.VERTICAL -> {
                    adjustVolumeOrLight(e1, e2, x, y)
                }
                LinearLayout.HORIZONTAL -> {
                    currentModel?.let {
                        if (it == gestureView.player?.getCurrentModel()) {
                            progress = hint.setSeekProgress(-swipeX / width, it,lastModelProgress)
                        } else {
                            hint.visibility = View.GONE
                        }
                    }
                }
                else -> {
                    val touchX = e1?.x ?: return@setOnScrollListener
                    val touchY = e1.y
                    if (checkTouchArea(touchX, touchY)) {
                        if (abs(x) > abs(y)) {
                            //横向滑动
                            scrollOrientation = LinearLayout.HORIZONTAL
//                            adjustProgress(e1, e2, x, y)
                        } else {
                            //纵向滑动
                            scrollOrientation = LinearLayout.VERTICAL
                            adjustVolumeOrLight(e1, e2, x, y)
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理纵向滑动事件
     */
    private fun adjustVolumeOrLight(e1: MotionEvent?, e2: MotionEvent?, x: Float, y: Float) {
        val downEvent = e1 ?: return
        if (downEvent.x > width.toFloat() / 2) {
            setVolume(y)
        } else {
            setScreenLight(y)
        }
    }

    /**
     * 检查是否在允许滑动区域
     * 由于全面屏手势，边缘部分不允许响应手势
     */
    private fun checkTouchArea(touchX: Float, touchY: Float): Boolean {
        val offX = width.toFloat() / 20
        val offY = height.toFloat() / 10
        return touchX > offX && touchX < (width - offX)
                && touchY > offY && touchY < (height - offY)
    }

    private fun setScreenLight(offY: Float) {
        val currentLight = gestureView.setScreenBrightnessSlide(offY / height)
        hint.setProgress("亮度", null, currentLight)
    }

    private fun setVolume(offY: Float) {
        val currentLight = gestureView.setVolume(mAudioManager, offY)
        hint.setProgress("音量", null, currentLight)
    }

    /**
     * 更新进度
     */
    fun updateProgress() {
        currentModel = null
        progress?.let {
            gestureView.player?.seekTo(it)
        }
    }
}