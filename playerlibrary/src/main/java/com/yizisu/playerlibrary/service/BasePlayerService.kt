package com.yizisu.playerlibrary.service

import android.app.Service
import android.content.*
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import com.google.android.exoplayer2.C
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener


class BasePlayerService<Model : PlayerModel> : Service(), SimplePlayerListener<Model> {
    companion object {
        private var servicePair: Bean? = null
        private var serviceCreateCall: (IYzsPlayer<PlayerModel>.() -> Unit)? = null
        fun <Model : PlayerModel> getPlayer(): IYzsPlayer<Model>? {
            return servicePair?.service?.player as? IYzsPlayer<Model>
        }

        fun <Model : PlayerModel> getPlayer(context: Context, call: IYzsPlayer<Model>.() -> Unit) {
            val player = getPlayer<Model>()
            if (player == null) {
                serviceCreateCall = call as IYzsPlayer<PlayerModel>.() -> Unit
                bind(context)
            } else {
                call.invoke(player)
            }
        }

        /**
         * 绑定服务
         */
        @Synchronized
        fun bind(context: Context) {
            if (servicePair != null) {
                return
            }
            val serviceConnection = HomeMusicConnection()
            servicePair?.service = null
            servicePair = Bean(null, serviceConnection)
            context.applicationContext.bindService(
                Intent(context, BasePlayerService::class.java),
                serviceConnection,
                BIND_AUTO_CREATE
            )
        }

        /**
         * 解绑服务
         */
        @Synchronized
        fun unBind(context: Context) {
            val connection = servicePair?.connection ?: return
            servicePair?.service = null
            servicePair = null
            context.applicationContext.unbindService(connection)
        }
    }

    private data class Bean(
        var service: BasePlayerService<PlayerModel>? = null,
        var connection: ServiceConnection
    )

    private class HomeMusicConnection : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

        }
    }

    //播放器对象
    private val player by lazy {
        IYzsPlayer<Model>(this, IYzsPlayer.Impl.EXO_PLAYER).apply {
            setWakeMode(C.WAKE_MODE_NETWORK)
            setAudioForceEnable(true)
        }
    }

    //通知栏按钮接收广播
    private val phoneEarDisconnectReceiver by lazy { PhoneEarDisconnectReceiver() }

    /**
     * 通知栏按键广播
     */
    inner class PhoneEarDisconnectReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    player.pause()
                }
            }
        }
    }

    override fun onTick(playerModel: Model) {

    }

    override fun onPlay(playStatus: Boolean, playerModel: Model?) {
        super.onPlay(playStatus, playerModel)
    }

    override fun onPause(playStatus: Boolean, playerModel: Model?) {
        super.onPause(playStatus, playerModel)
    }

    override fun onPlayerModelChange(playerModel: Model) {
        super.onPlayerModelChange(playerModel)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        create()
    }


    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    private fun destroy() {
        servicePair?.service = null
        servicePair = null
        player.onDestroy()
        unregisterReceiver(phoneEarDisconnectReceiver)
    }

    private fun create() {
        player.addPlayerListener(this)
        servicePair?.service = this as BasePlayerService<PlayerModel>
        serviceCreateCall?.invoke(player)
        serviceCreateCall = null
        registerReceiver(phoneEarDisconnectReceiver, IntentFilter().apply {
            //耳机断开
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        })
    }
}
