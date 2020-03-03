package com.yizisu.playerlibrary

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

interface PlayerLifecycleObserver : LifecycleObserver {
    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
    }

    /**
     * 可以不需要参数
     * 或者需要一个参数
     * 或者最多两个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onLifeChange(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event) {
    }
}