package com.yizisu.playerlibrary.impl.exoplayer

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.TextureView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.video.VideoListener
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.impl.BaseYzsPlayer
import com.yizisu.playerlibrary.helper.logI

/**
 * ExoPlayer实现类，可以用于其他播放器替换
 */
class ExoPlayerImpl(private val context: Context) : BaseYzsPlayer(context), Player.EventListener,
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
        models: MutableList<PlayerModel>,
        playIndex: Int,
        isStopLastMedia: Boolean,
        listener: ((PlayerModel?) -> Unit)?
    ) {
        play(null)
        prepare(models, playIndex, isStopLastMedia, listener)
    }

    override fun prepare(
        models: MutableList<PlayerModel>,
        playIndex: Int,
        isStopLastMedia: Boolean,
        listener: ((PlayerModel?) -> Unit)?
    ) {
        super.prepare(models, playIndex, isStopLastMedia, listener)
        startPrepare(listener, isStopLastMedia)
    }

    override fun play(listener: ((PlayerModel?) -> Unit)?) {
        //判断是否启用了音频焦点处理
        if (!requestAudioFocus()) {
            player.playWhenReady = true
        }
        listener?.invoke(currentPlayModel)
    }

    override fun stop(listener: ((PlayerModel?) -> Unit)?) {
        super.stop(listener)
        pause(listener)
        player.stop()
        doPlayerListener {
            it.onStop(player.playWhenReady, currentPlayModel)
        }
    }

    override fun pause(listener: ((PlayerModel?) -> Unit)?) {
        super.pause(listener)
        player.playWhenReady = false
        listener?.invoke(currentPlayModel)
    }

    override fun next(listener: ((PlayerModel?) -> Unit)?) {
        currentIndex++
        startPrepare(listener, true)
    }

    /**
     * 准备资源和播放
     */
    private fun startPrepare(
        listener: ((PlayerModel?) -> Unit)?,
        isStopLastMedia: Boolean
    ) {
        if (isStopLastMedia) {
            player.stop(true)
        }
        val playModel = currentPlayModel?.apply {
            player.createSingleSource(context, this)
            doPlayerListener {
                it.onPlayerModelChange(this)
            }
        }
        listener?.invoke(playModel)
    }

    override fun previous(listener: ((PlayerModel?) -> Unit)?) {
        currentIndex--
        startPrepare(listener, true)
    }

    override fun attachView(view: TextureView) {
        player.setVideoTextureView(view)
    }

    override fun seekTo(positionMs: Long, index: Int?, listener: ((PlayerModel?) -> Unit)?) {
        if (index == null) {
            player.seekTo(positionMs)
        } else {
            if (currentIndex == index) {
                listener?.invoke(currentPlayModel)
                return
            }
            //注意这里，这里赋值，不受播放模式判断影响，所以直接赋值
            _currentIndex = index
            currentPlayModel?.apply {
                player.createSingleSource(context, this)
            }
            listener?.invoke(currentPlayModel)
        }
    }

    /**
     * 播放出错
     */
    override fun onPlayerError(error: ExoPlaybackException) {
        logI(error.message)
        doPlayerListener {
            it.onError(error, currentPlayModel)
        }
    }

    override fun isPlaying(): Boolean {
        return player.playWhenReady
    }

    override fun setMediaSession(
        session: MediaSessionCompat,
        bundleCall: (PlayerModel?) -> MediaDescriptionCompat
    ) {
        MediaSessionConnector(session).apply {
            setQueueNavigator(QueueNavigator(mediaSession, bundleCall))
            setPlayer(player)
        }
    }

    private inner class QueueNavigator(
        mediaSession: MediaSessionCompat,
        val bundleCall: (PlayerModel?) -> MediaDescriptionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return bundleCall(currentPlayModel)
        }
    }

    override fun setHandleWakeLock(handleWakeLock: Boolean) {
        player.setHandleWakeLock(true)
    }

    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    override fun getVolume(volume: Float): Float {
        return player.volume
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