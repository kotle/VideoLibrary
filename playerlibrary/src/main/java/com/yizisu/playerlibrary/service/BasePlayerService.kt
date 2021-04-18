package com.yizisu.playerlibrary.service

import android.app.Service
import android.content.*
import android.media.AudioManager
import android.os.IBinder
import com.google.android.exoplayer2.C
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener

/**
 * 请使用[PlayerServiceHelper]来创建对象
 */
open class BasePlayerService<Model : PlayerModel> : Service(), SimplePlayerListener<Model> {
    internal var onCreateListener: Function1<BasePlayerService<*>?, Unit>? = null

    //播放器对象
    val player by lazy {
        IYzsPlayer<Model>(this).apply {
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
        return PlayerServiceBinder(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        onCreateListener?.invoke(this)
        create()
    }


    override fun onDestroy() {
        destroy()
        onCreateListener?.invoke(null)
        onCreateListener = null
        super.onDestroy()
    }

    private fun destroy() {
        player.onDestroy()
        unregisterReceiver(phoneEarDisconnectReceiver)
    }

    private fun create() {
        player.addPlayerListener(this)
        registerReceiver(phoneEarDisconnectReceiver, IntentFilter().apply {
            //耳机断开
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        })
    }
}
