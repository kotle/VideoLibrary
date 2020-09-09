package com.yizisu.playerlibrary.impl.exoplayer

import android.content.Context
import android.media.AudioManager
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.TextureView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.video.VideoListener
import com.yizisu.playerlibrary.PlayerFactory
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.helper.logI
import com.yizisu.playerlibrary.impl.BaseYzsPlayer

/**
 * ExoPlayer实现类，可以用于其他播放器替换
 */
internal class ExoPlayerImpl<Model : PlayerModel>(private val context: Context) :
    BaseYzsPlayer<Model>(context), Player.EventListener,
    VideoListener, AudioListener {
    //创建播放器
    private val player = createSimpleExoPlayer(context).apply {
        addListener(this@ExoPlayerImpl)
        addVideoListener(this@ExoPlayerImpl)
    }

    override val totalDuration: Long
        get() = player.duration

    override val currentDuration: Long
        get() = player.currentPosition

    override val currentBufferDuration: Long
        get() = player.bufferedPosition

    override fun prepareAndPlay(
        models: MutableList<Model>,
        playIndex: Int,
        isStopLastMedia: Boolean,
        listener: ((Model?) -> Unit)?
    ) {
        play(null)
        prepare(models, playIndex, isStopLastMedia, listener)
    }

    override fun prepare(
        models: MutableList<Model>,
        playIndex: Int,
        isStopLastMedia: Boolean,
        listener: ((Model?) -> Unit)?
    ) {
        super.prepare(models, playIndex, isStopLastMedia, listener)
        startPrepare(listener, isStopLastMedia)
    }

    override fun play(listener: ((Model?) -> Unit)?) {
        super.play(listener)
        //判断是否启用了音频焦点处理
        if (!requestAudioFocus()) {
            player.playWhenReady = true
        }
        listener?.invoke(currentPlayModel)
    }

    override fun stop(listener: ((Model?) -> Unit)?) {
        super.stop(listener)
        pause(listener)
        player.stop()
        doPlayerListener {
            it.onStop(player.playWhenReady, currentPlayModel)
        }
    }

    override fun pause(listener: ((Model?) -> Unit)?) {
        super.pause(listener)
        player.playWhenReady = false
        listener?.invoke(currentPlayModel)
    }

    override fun next(listener: ((Model?) -> Unit)?) {
        val index = getCurrentPlayIndex()
        //如果播放的是最后一个，播放完之后就停止播放
        if (getRepeatMode() == PlayerFactory.LOOP_MODO_NONE && (index + 1) >= playModelList.count()) {
            stop()
        } else {
            currentIndex++
            startPrepare(listener, true)
        }
    }


    /**
     * 准备资源和播放
     */
    private var lastPlayModel: PlayerModel? = null
    private fun startPrepare(
        listener: ((Model?) -> Unit)?,
        isStopLastMedia: Boolean
    ) {
        //切换资源的时候，回调上一次资源的销毁方法，做好资源回收
        if (lastPlayModel != currentPlayModel) {
            lastPlayModel?.onPlayModelNotThis()
            lastPlayModel = currentPlayModel
        }
        if (isStopLastMedia) {
            player.stop(true)
        }
        val playModel = currentPlayModel?.apply {
            player.createSingleSource(
                context,
                this,
                this@ExoPlayerImpl
            ) { error, isCallOnPlayChange ->
                //错误监听
                if (error != null) {
                    doPlayerListener {
                        currentPlayModel?.onError(error)
                        it.onError(error, currentPlayModel)
                    }
                } else {
                    //因为获取url是异步的，而且还有可能从网络获取其他信息
                    //所以获取url后再通知一次
                    if (isCallOnPlayChange) {
                        currentPlayModel?.let { model ->
                            doPlayerListener {
                                it.onPlayerModelChange(model)
                            }
                        }
                    }
                }
            }
            doPlayerListener {
                it.onPlayerModelChange(this)
            }
        }
        listener?.invoke(playModel)
    }

    override fun previous(listener: ((Model?) -> Unit)?) {
        currentIndex--
        startPrepare(listener, true)
    }

    override fun attachView(view: TextureView?) {
        player.setVideoTextureView(view)
    }

    override fun clearView(view: TextureView?) {
        player.clearVideoTextureView(view)
    }

    override fun seekTo(positionMs: Long, index: Int?, listener: ((Model?) -> Unit)?) {
        if (index == null) {
            player.seekTo(positionMs)
        } else {
            if (currentIndex == index) {
                listener?.invoke(currentPlayModel)
                return
            }
            //注意这里，这里赋值，不受播放模式判断影响，所以直接赋值
            _currentIndex = index
            startPrepare(listener, true)
        }
    }

    override fun seekRatioTo(ratio: Float, listener: ((Model?) -> Unit)?) {
        seekTo((totalDuration * ratio).toLong(), null, listener)
    }

    /**
     * 播放出错
     */
    override fun onPlayerError(error: ExoPlaybackException) {
        logI(error.message)
        doPlayerListener {
            currentPlayModel?.onError(error)
            it.onError(error, currentPlayModel)
        }
    }

    override fun isPlaying(): Boolean {
        return player.playWhenReady
    }

    override fun setMediaSession(
        session: MediaSessionCompat,
        bundleCall: (Model?) -> MediaDescriptionCompat
    ) {
        MediaSessionConnector(session).apply {
            setQueueNavigator(QueueNavigator(mediaSession, bundleCall))
            setPlayer(player)
        }
    }

    private inner class QueueNavigator(
        mediaSession: MediaSessionCompat,
        val bundleCall: (Model?) -> MediaDescriptionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return bundleCall(currentPlayModel)
        }
    }

    override fun setSingleModelRepeatModel(@PlayerFactory.RepeatMode repeatMode: Int) {
        player.repeatMode = repeatMode
    }

    override fun setHandleWakeLock(handleWakeLock: Boolean) {
        player.setHandleWakeLock(true)
    }

    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    override fun getVolume(): Float {
        return player.volume
    }

    override fun setVideoSpeed(speed: Float) {
        player.setPlaybackParameters(PlaybackParameters(speed))
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop(true)
        player.release()
    }

    override fun getAudioForceListener(): AudioManager.OnAudioFocusChangeListener {
        return audioFocusListener
    }

    /**
     * EXOPlayer监听-播放状态改变
     */
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            doPlayerListener {
                it.onBufferStateChange(true, playWhenReady, currentPlayModel)
            }
        } else {
            doPlayerListener {
                it.onBufferStateChange(false, playWhenReady, currentPlayModel)
            }
        }
        val state = when (playbackState) {
            Player.STATE_BUFFERING -> {
                "STATE_BUFFERING"
            }
            Player.STATE_ENDED -> {
                next()
                "STATE_ENDED"
            }
            Player.STATE_IDLE -> {
                "STATE_IDLE"
            }
            Player.STATE_READY -> {
                "STATE_READY"
            }
            else -> {
                "null"
            }
        }
        logI("--->${playWhenReady}:${state}")
    }

    /**
     * EXOPlayer监听-播放状态变化
     */
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        logI("--->isPlaying:${isPlaying}")
        if (isPlaying) {
            doPlayerListener {
                it.onPlay(player.playWhenReady, currentPlayModel)
            }
        } else {
            doPlayerListener {
                it.onPause(player.playWhenReady, currentPlayModel)
            }
        }
    }

    /**
     * EXOPlayer监听-视频大小变化
     */
    override fun onVideoSizeChanged(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        currentPlayModel?._videoWidth = width
        currentPlayModel?._videoHeight = height
        doPlayerListener {
            it.onVideoSizeChange(
                width,
                height,
                unappliedRotationDegrees,
                pixelWidthHeightRatio,
                currentPlayModel
            )
        }
    }

    /**
     * 音频焦点处理
     * 每个对象都会获取一个焦点，创建的其他对象会失去焦点
     */
    private val audioFocusListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            doPlayerListener {
                it.onAudioFocusChange(focusChange)
            }
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    player.playWhenReady = true
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    player.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    player.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    player.playWhenReady = false
                }
            }
        }
}