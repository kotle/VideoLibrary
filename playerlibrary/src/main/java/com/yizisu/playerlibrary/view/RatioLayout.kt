package com.yizisu.playerlibrary.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.LinearInterpolator


/**
 * 此布局必须有固定的宽高
 * 根据比例布局子view
 */
internal open class RatioLayout : ViewGroup {

    private var lastChildRatio = 1f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (!changed) {
            return
        }
        val width = right - left
        val height = bottom - top
        getChildParams(width, height, lastChildRatio)
    }

    private fun getChildParams(width: Int, height: Int, childRatio: Float) {
        val parentRatio = width.toFloat() / height
        if (childRatio <= parentRatio) {
            //child 高度不变
            val childWidth = height * childRatio
            val childTop = 0
            val off = (width - childWidth) / 2
            val childLeft = off.toInt()
            val childRight = (width - off).toInt()
            childLayout(childLeft, childTop, childRight, height)
        } else {
            //宽度不变
            val childHeight = width / childRatio
            val off = (height - childHeight) / 2
            val childTop = off.toInt()
            val childLeft = 0
            val childBottom = (height - off).toInt()
            childLayout(childLeft, childTop, width, childBottom)
        }
    }

    private fun childLayout(left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        repeat(count) {
            getChildAt(it).layout(left, top, right, bottom)
        }
    }

    private fun setChildRatio(childRatio: Float, isReLayout: Boolean) {
        if (lastChildRatio == childRatio) {
            return
        }
        lastChildRatio = childRatio
        if (isReLayout) {
            if (width > 0 && height > 0) {
                getChildParams(width, height, lastChildRatio)
            }
        }
    }

    /*********************************************************************/
    private var lastAnim: ValueAnimator? = null
    fun smoothChangeChildRatio(childRatio: Float, animDuration: Long = 200) {
        if (lastChildRatio == childRatio) {
            return
        }
        val viewWidth = width
        val viewHeight = height
        if (viewWidth > 0 && viewHeight > 0) {
            lastAnim?.cancel()
            lastAnim = ValueAnimator.ofFloat(lastChildRatio, childRatio).apply {
                duration = animDuration
                val listener = object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animator: Animator) {

                    }

                    override fun onAnimationEnd(animator: Animator) {
                        setChildRatio(childRatio, false)
                        lastAnim = null
                    }

                    override fun onAnimationCancel(animator: Animator) {

                    }

                    override fun onAnimationStart(animator: Animator) {

                    }
                }
                addListener(listener)
                interpolator = LinearInterpolator()
                addUpdateListener {
                    val ratio = it.animatedValue as Float
                    getChildParams(viewWidth, viewHeight, ratio)
                }
                start()
            }
        }
    }

    fun setChildRatio(childRatio: Float) {
        setChildRatio(childRatio, true)
    }
}