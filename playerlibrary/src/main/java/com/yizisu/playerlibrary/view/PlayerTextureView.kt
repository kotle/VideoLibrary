package com.yizisu.playerlibrary.view

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.yizisu.playerlibrary.PlayerLifecycleObserver

/**
 * 处理锁屏之后，导致黑屏
 */
open class PlayerTextureView : TextureView, PlayerLifecycleObserver {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val ctx = context
        if (ctx is AppCompatActivity) {
            ctx.lifecycle.addObserver(this)
        } else if (ctx is ContextWrapper) {
            val c = ctx.baseContext
            if (c is AppCompatActivity) {
                c.lifecycle.addObserver(this)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val ctx = context
        if (ctx is AppCompatActivity) {
            ctx.lifecycle.removeObserver(this)
        } else if (ctx is ContextWrapper) {
            val c = ctx.baseContext
            if (c is AppCompatActivity) {
                c.lifecycle.removeObserver(this)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if (this.visibility == View.VISIBLE) {
            this.visibility = View.INVISIBLE
            this.visibility = View.VISIBLE
        }
    }
}