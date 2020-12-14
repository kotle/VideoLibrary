package com.yizisu.playerlibrary.service

import android.app.Service
import android.content.Context
import android.content.Intent
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel

/**
 * 记得需要在销毁的时候解绑服务
 */
class PlayerServiceHelper<MODEL : PlayerModel, SERVICE : BasePlayerService<MODEL>>(private val serviceCls: Class<SERVICE>) {

    private var serviceConnection: PlayerServiceConnection? = null
    private fun clear() {
        serviceConnection?.service = null
        serviceConnection = null
    }
    //--------------------------------------------------------------------------------//
    /**
     * 绑定服务
     */
    fun bind(context: Context, bindListener: Function1<Boolean, Unit>? = null) {
        if (serviceConnection != null) {
            serviceConnection?.bindListener = {
                bindListener?.invoke(it)
                if (!it) {
                    clear()
                }
            }
            return
        }
        val connection = PlayerServiceConnection()
        connection.bindListener = {
            bindListener?.invoke(it)
            if (!it) {
                clear()
            }
        }
        serviceConnection = connection
        context.bindService(
            Intent(context, serviceCls),
            connection,
            Service.BIND_AUTO_CREATE
        )
    }

    /**
     * 解绑服务
     */
    fun unBind(context: Context) {
        val connection = serviceConnection
        if (connection != null) {
            context.unbindService(connection)
            clear()
        }
    }


    //---------------------------------------------------------------------//
    /**
     * 获取创建的服务
     */
    @Suppress("UNCHECKED_CAST")
    val service: SERVICE?
        get() = serviceConnection?.service as? SERVICE

    /**
     * 如果为创建或者未启动服务，会先启动服务
     */
    fun getService(context: Context, listener: Function1<SERVICE, Unit>) {
        val service = this.service
        if (service != null) {
            listener.invoke(service)
        } else {
            bind(context) {
                if (it) {
                    getService(context, listener)
                }
            }
        }
    }

    /**
     * 获取播放器
     */
    val player: IYzsPlayer<MODEL>?
        get() = service?.player

    fun getPlayer(context: Context, listener: Function1<IYzsPlayer<MODEL>, Unit>) {
        getService(context) {
            listener.invoke(it.player)
        }
    }
}