package com.yizisu.playerlibrary.impl.exoplayer

import android.content.Context
import android.view.TextureView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoListener
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.impl.BaseYzsPlayer
import com.yizisu.playerlibrary.helper.logI


/**
 * ExoPlayer实现类，可以用于其他播放器替换
 */
class ExoPlayerImpl(private val context: Context) : BaseYzsPlayer(), Player.EventListener,
    VideoListener {
    /**
     * 利用播放器的上一首下一首，如果播放链接出错，会导致此播放器瘫痪
     * 所以我现在写一个自己的播放索引
     */
    private var isUsePlayerSeekList = false
    override var currentIndex: Int
        get() {
            return if (isUsePlayerSeekList) {
                player.currentWindowIndex
            } else {
                super.currentIndex
            }
        }
        set(value) {
            super.currentIndex = value
        }
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

    override val bufferedPercentage: Int
        get() = player.bufferedPercentage

    override fun prepareAndPlay(
        models: MutableList<PlayerModel>,
        listener: ((PlayerModel?) -> Unit)?
    ) {
        prepare(models, null)
        play(listener)
    }

    override fun prepare(models: MutableList<PlayerModel>, listener: ((PlayerModel?) -> Unit)?) {
        super.prepare(models, listener)
        player.createDefaultSource(context, models)
        listener?.invoke(currentPlayModel)
    }

    override fun play(listener: ((PlayerModel?) -> Unit)?) {
        player.playWhenReady = true
        listener?.invoke(currentPlayModel)
    }

    override fun stop(listener: ((PlayerModel?) -> Unit)?) {
        player.stop()
    }

    override fun pause(listener: ((PlayerModel?) -> Unit)?) {
        player.playWhenReady = false
        listener?.invoke(currentPlayModel)
    }

    override fun next(listener: ((PlayerModel?) -> Unit)?) {
        currentIndex++
        if (isUsePlayerSeekList) {
            if (player.hasNext()) {
                player.next()
                listener?.invoke(currentPlayModel)
            } else {
                listener?.invoke(null)
            }
        } else {
            listener?.invoke(currentPlayModel?.apply {
                player.createSingleSource(context, this)
            })
        }
    }

    override fun previous(listener: ((PlayerModel?) -> Unit)?) {
        currentIndex--
        if (isUsePlayerSeekList) {
            if (player.hasPrevious()) {
                player.previous()
                listener?.invoke(currentPlayModel)
            } else {
                listener?.invoke(null)
            }
        } else {
            listener?.invoke(currentPlayModel?.apply {
                player.createSingleSource(context, this)
            })
        }

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
            if (isUsePlayerSeekList) {
                if (index < playModelList.count()) {
                    player.seekTo(index, positionMs)
                }
            } else {
                currentPlayModel?.apply {
                    player.createSingleSource(context, this)
                }
            }
            listener?.invoke(currentPlayModel)
        }
    }

    /**
     * 播放出错
     */
    override fun onPlayerError(error: ExoPlaybackException) {
        logI(error.message)
        player.duration
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop(true)
        player.release()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
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

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        logI("--->isPlaying:${isPlaying}")
    }
}