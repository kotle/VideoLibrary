package com.yizisu.playerlibrary.helper

import android.content.Context
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi

/**
 * 手势检测器
 * view:需要处理的view
 * isSetOnTouchListener：是否调用setOnTouchListener方法
 */
internal class GestureDetectorHelper(private val view: View, isSetOnTouchListener: Boolean) :
    GestureDetector.SimpleOnGestureListener() {
    private val gestureDetector = createGestureDetector(view.context)

    init {
        if (isSetOnTouchListener) {
            view.setOnTouchListener { _, event ->
                return@setOnTouchListener gestureDetector.onTouchEvent(event)
            }
        }
    }

    //单击
    private var onClickListener: Function1<MotionEvent?,Unit>? = null
    //长按
    private var onLongClickListener: Function1<MotionEvent?,Unit>? = null
    //双击
    private var onDoubleClickListener: Function1<MotionEvent?,Unit>? = null
    //滑动
    private var onScrollListener: Function4<MotionEvent?, MotionEvent?, Float, Float, Unit>? = null

    /**
     * 创建真正的手势检测器
     */
    private fun createGestureDetector(context: Context): GestureDetector {
        return GestureDetector(context, this).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setContextClickListener(this@GestureDetectorHelper)
            }
            setOnDoubleTapListener(this@GestureDetectorHelper)
        }
    }

    /**
     * 是否可以长按
     */
    var isLongpressEnabled: Boolean
        set(value) {
            gestureDetector.setIsLongpressEnabled(value)
        }
        get() {
            return gestureDetector.isLongpressEnabled
        }

    /**
     * 设置单击事件
     */
    fun setOnClickListener(l: Function1<MotionEvent?,Unit>?) {
        onClickListener = l
    }

    /**
     * 设置双击事件
     */
    fun setOnDoubleClickListener(l: Function1<MotionEvent?,Unit>?) {
        onDoubleClickListener = l
    }

    /**
     * 设置长按事件
     */
    fun setOnLongClickListener(l: Function1<MotionEvent?,Unit>?) {
        onLongClickListener = l
    }

    /**
     * 设置滑动
     */
    fun setOnScrollListener(l: Function4<MotionEvent?, MotionEvent?, Float, Float, Unit>?) {
        onScrollListener = l
    }

    fun onTouchEvent(ev: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(ev)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onGenericMotionEvent(ev: MotionEvent?) {
        gestureDetector.onGenericMotionEvent(ev)
    }

    //****************************************手势检测回调start**************************************
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
//        logI("onSingleTapUp")
        return super.onSingleTapUp(e)
    }

    override fun onDown(e: MotionEvent?): Boolean {
//        logI("onDown")
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
//        logI("onDoubleTap")
        onDoubleClickListener?.invoke(e)
        return super.onDoubleTap(e)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        onScrollListener?.invoke(e1, e2, distanceX, distanceY)
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onContextClick(e: MotionEvent?): Boolean {
//        logI("onContextClick：${e?.toString()}")
        return super.onContextClick(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
//        logI("onSingleTapConfirmed：${e?.toString()}")
        onClickListener?.invoke(e)
        return super.onSingleTapConfirmed(e)
    }

    override fun onShowPress(e: MotionEvent?) {
//        logI("onShowPress")
        super.onShowPress(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
//        logI("onDoubleTapEvent：${e?.toString()}")
        return super.onDoubleTapEvent(e)
    }

    override fun onLongPress(e: MotionEvent?) {
//        logI("onLongPress")
        onLongClickListener?.invoke(e)
        super.onLongPress(e)
    }
    //****************************************手势检测回调end**************************************
}