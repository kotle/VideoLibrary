package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.getCountTimeByLong
import kotlin.math.min

/**
 * 中间弹窗，提示view
 * 调节亮度，声音和进度
 */
class MidProgressHintView : LinearLayout {

    //手指滑动的视频总长度,从左滑到有，最多可以滑动进度
    //横屏十分钟，竖屏五分钟
    private val maxDurationPoint = Point(600_000, 300_000)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    )

    private val progressBar: ProgressBar
    private val titleTv: TextView
    private val hintTv: TextView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        LayoutInflater.from(context).inflate(R.layout.video_player_mid_hint_progress, this, true)
        progressBar = findViewById(R.id.progress)
        titleTv = findViewById(R.id.titleTv)
        hintTv = findViewById(R.id.hintTv)
    }


    private fun setProgress(title: String?, hint: String?, progress: Int, max: Int) {
        progressBar.max = max
        progressBar.progress = progress
        titleTv.text = title
        hintTv.text = hint
        visibility = VISIBLE
    }

    fun setProgress(title: String?, hint: String?, r: Float) {
        setProgress(title, hint, (1000 * r).toInt(), 1000)
    }

    fun setSeekProgress(swipe: Float, model: PlayerModel, lastModelProgress: Long?): Long {
        val lastProgress = lastModelProgress ?: model.currentDuration
        if (model.totalDuration == 0L) {
            return lastProgress
        }
        val swipeDuration = getSwipeMaxDuration(model.totalDuration) * swipe
        val progress = (lastProgress + swipeDuration).toLong()
        setProgress(
                "进度",
                "${getCountTimeByLong(progress)}/${getCountTimeByLong(model.totalDuration)}",
                progress.toInt(),
                model.totalDuration.toInt()
        )
        return progress
    }

    /**
     * 获取可以滑动的最大长度
     * 横屏10分钟 竖屏5分钟
     *
     */
    private fun getSwipeMaxDuration(totalVideoDuration: Long): Long {
        var ctx = context
        if (ctx is ContextThemeWrapper) {
            //是在对话框
            ctx = ctx.baseContext
        }
        return if (ctx is AppCompatActivity) {
            if (ctx.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                    ctx.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            ) {
                //竖屏
                min(totalVideoDuration, maxDurationPoint.x.toLong())
            } else {
                //横屏
                min(totalVideoDuration, maxDurationPoint.y.toLong())
            }
        } else {
            min(totalVideoDuration, maxDurationPoint.y.toLong())
        }
    }
}