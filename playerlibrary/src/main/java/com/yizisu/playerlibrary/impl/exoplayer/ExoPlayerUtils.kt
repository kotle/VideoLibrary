package com.yizisu.playerlibrary.impl.exoplayer

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.yizisu.playerlibrary.BuildConfig
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.logI


/**
 * 创建默认的播放资源
 */
/*internal fun SimpleExoPlayer.createDefaultSource(
    context: Context,
    models: MutableList<PlayerModel>
): MediaSource {
    if (models.isEmpty()) {
        throw IllegalArgumentException("PlayerModel 集合不能为kong")
    }
    val source = if (models.count() == 1) {
        models[0].buildMediaSource(context)
    } else {
        ConcatenatingMediaSource(*models.map {
            it.buildMediaSource(context)
        }.toTypedArray())
    }
    prepare(source)
    return source
}*/

/**
 * 创建MediaSource
 */
internal fun SimpleExoPlayer.createSingleSource(
    context: Context,
    model: PlayerModel,
    exoPlayerImpl: ExoPlayerImpl<*>,
    errorListener: Function2<Throwable?, Boolean, Unit>
) {
    model.callMediaItem { mediaItem, error, isCallOnPlayChange ->
        if (model != exoPlayerImpl.currentPlayModel) {
            return@callMediaItem
        }
        if (BuildConfig.DEBUG) {
            logI("ExoPlay播放准备资源：url:${mediaItem?.playbackProperties?.uri}\nerror:${error?.message}\nisCallOnPlayChange:${isCallOnPlayChange}")
        }
        context.runOnUiThread {
            if (mediaItem == null) {
                if (error == null) {
                    errorListener.invoke(Throwable("Uri is null"), false)
                } else {
                    errorListener.invoke(error, false)
                }
            } else {
                setMediaSource(model.buildMediaSource(mediaItem, context))
                prepare()
                errorListener.invoke(null, isCallOnPlayChange)
            }
        }
    }
}

/**
 * 创建exoPlayer播放器
 */
internal fun createSimpleExoPlayer(context: Context): SimpleExoPlayer {
    return SimpleExoPlayer
        .Builder(context)
//        .setLoadControl(MyLoadController())
        .build()
}

class MyLoadController : DefaultLoadControl() {
    /**
     * 返回缓存时间
     */
    override fun getBackBufferDurationUs(): Long {
        return C.msToUs(30_60_1000)
    }
}


internal val mainHandler = Handler(Looper.getMainLooper())

/**
 * 是否是主线程
 */
private fun isMainThread() = Looper.getMainLooper() === Looper.myLooper()

/**
 * 代码运行在主线程
 */
private fun Context.runOnUiThread(f: Context.() -> Unit) {
    if (isMainThread()) f() else mainHandler.post { f() }
}

private fun PlayerModel.buildMediaSource(mediaUri: MediaItem, context: Context): MediaSource {
    //判断是否是rtmp流,已经测试支持rtmp
    if (isRtmpSource(mediaUri, overrideExtension)) {
        return ProgressiveMediaSource.Factory(
            RtmpDataSourceFactory()
        ).createMediaSource(mediaUri)
    }
    //判断是否是rtsp流
//    if (isRtspSource(getMediaUri(), overrideExtension)) {
//        return HlsMediaSource.Factory(
//            DefaultHttpDataSourceFactory(Util.getUserAgent(context, context.packageName))
//        ).createMediaSource(getMediaUri())
//    }
    if (isRtmpSource(mediaUri, overrideExtension)) {
        return ProgressiveMediaSource.Factory(
            RtmpDataSourceFactory()
        ).createMediaSource(mediaUri)
    }
    return when (val type = Util.inferContentType(
        mediaUri.playbackProperties?.uri
            ?: Uri.EMPTY, overrideExtension
    )) {
        C.TYPE_SS -> {
            SsMediaSource.Factory(
                getDefaultHttpDataSourceFactory(context)
            ).createMediaSource(mediaUri)
        }
        C.TYPE_DASH -> {
            DashMediaSource.Factory(
                getDefaultHttpDataSourceFactory(context)
            ).createMediaSource(mediaUri)
        }
        C.TYPE_HLS -> {
            HlsMediaSource.Factory(
                getDefaultHttpDataSourceFactory(context)
            ).createMediaSource(mediaUri)
        }
        C.TYPE_OTHER -> {
            ProgressiveMediaSource.Factory(
                if (mediaUri.playbackProperties?.uri?.scheme == "http" || mediaUri.playbackProperties?.uri?.scheme == "https") {
                    getDefaultHttpDataSourceFactory(context)
                } else {
                    DefaultDataSourceFactory(
                        context,
                        Util.getUserAgent(context, context.packageName)
                    )
                }
            ).createMediaSource(mediaUri)
        }
        else -> {
            throw IllegalArgumentException("Unsupported type: $type")
        }
    }
}

private fun getDefaultHttpDataSourceFactory(context: Context): DefaultHttpDataSource.Factory {
    return DefaultHttpDataSource.Factory().apply {
        setUserAgent(Util.getUserAgent(context, context.packageName))
        setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
        setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
        setAllowCrossProtocolRedirects(true/*允许重定向*/)
    }
}

private fun isRtmpSource(uri: MediaItem, overrideExtension: String?): Boolean {
    return overrideExtension?.contains("rtmp") == true || uri.toString().startsWith("rtmp")
}

private fun isRtspSource(uri: Uri, overrideExtension: String?): Boolean {
    return overrideExtension?.contains("rtsp") == true || uri.toString().startsWith("rtsp")
}

/**
 * 毫秒换成00:00:00
 */
internal fun getCountTimeByLong(finishTime: Long): String {
    if (finishTime <= 0) {
        return "00:00"
    }
    var totalTime = (finishTime / 1000).toInt()//秒
    var hour = 0
    var minute = 0
    var second = 0

    if (3600 <= totalTime) {
        hour = totalTime / 3600
        totalTime -= 3600 * hour
    }
    if (60 <= totalTime) {
        minute = totalTime / 60
        totalTime -= 60 * minute
    }
    if (0 <= totalTime) {
        second = totalTime
    }
    val sb = StringBuilder()
    if (hour > 0) {
        if (hour < 10) {
            sb.append("0").append(hour).append(":")
        } else {
            sb.append(hour).append(":")
        }
    }
    if (minute < 10) {
        sb.append("0").append(minute).append(":")
    } else {
        sb.append(minute).append(":")
    }
    if (second < 10) {
        sb.append("0").append(second)
    } else {
        sb.append(second)
    }
    return sb.toString()
}