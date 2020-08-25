package com.yizisu.playerlibrary.helper

import com.yizisu.playerlibrary.impl.exoplayer.mainHandler


class AutoTickTask  constructor(
    private val runnable: AutoTickTask.() -> Unit,
    private val time: Long
) :
    Runnable {
    companion object {

        fun start(time: Long = 1000L, run: AutoTickTask.() -> Unit): AutoTickTask {
            return AutoTickTask(run, time).also {
                it.run()
            }
        }

        fun AutoTickTask.stop() {
            cancel()
        }
    }

    override fun run() {
        if (mainHandler.hasCallbacks(this)) {
            return
        }
        mainHandler.postDelayed(this, time)
        runnable.invoke(this)
    }

    fun cancel() {
        mainHandler.removeCallbacks(this)
    }
}
