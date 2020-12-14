package com.yizisu.playerlibrary.service

import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * 需要keep注解，不然混淆之后子类的方法不回调（之前版本没问题，不知哪个版本出现这个问题）
 * 如果不用keep注解，子类需要加上父类注解
 * 执行顺序如下
 *
 * --->onCreate()
 * --->onCreate(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange()
 *
 * --->onStart()
 * --->onStart(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange()
 *
 * --->onResume()
 * --->onResume(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange()
 *
 * --->onPause(lifecycleOwner: LifecycleOwner)
 * --->onPause()
 * --->onLifeChange(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange()
 *
 * --->onStop(lifecycleOwner: LifecycleOwner)
 * --->onStop()
 * --->onLifeChange(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange()
 *
 * --->onDestroy(lifecycleOwner: LifecycleOwner)
 * --->onDestroy()
 * --->onLifeChange(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event)
 * --->onLifeChange(lifecycleOwner: LifecycleOwner)
 * --->onLifeChange()
 */
@Keep
interface IPlayerLifecycleObserver : LifecycleObserver {
    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(lifecycleOwner: LifecycleOwner) {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart(lifecycleOwner: LifecycleOwner) {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(lifecycleOwner: LifecycleOwner) {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause(lifecycleOwner: LifecycleOwner) {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop(lifecycleOwner: LifecycleOwner) {
    }

    /**
     * 可以不需要参数
     * 或者最多需要一个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(lifecycleOwner: LifecycleOwner) {
    }

    /**
     * 可以不需要参数
     * 或者需要一个参数
     * 或者最多两个参数
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onLifeChange() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onLifeChange(lifecycleOwner: LifecycleOwner) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onLifeChange(lifecycleOwner: LifecycleOwner, event: Lifecycle.Event) {
    }
}