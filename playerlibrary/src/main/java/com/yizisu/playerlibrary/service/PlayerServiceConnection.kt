package com.yizisu.playerlibrary.service

import android.app.Service
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

internal class PlayerServiceConnection : ServiceConnection {
    var service: Service? = null

    //绑定和解绑的监听
    var bindListener: Function1<Boolean, Unit>? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        this.service = null
        bindListener?.invoke(false)
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        val service = (binder as? PlayerServiceBinder)?.service
        this.service = service
        bindListener?.invoke(true)
        service?.lifeListener = {
            this.service = null
            bindListener?.invoke(it != null)
        }
    }
}