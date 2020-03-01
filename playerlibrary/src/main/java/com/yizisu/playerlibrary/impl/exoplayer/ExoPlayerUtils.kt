package com.yizisu.playerlibrary.impl.exoplayer

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.yizisu.playerlibrary.helper.PlayerModel


/**
 * 创建默认的播放资源
 */
internal fun SimpleExoPlayer.createDefaultSource(
    context: Context,
    models: MutableList<PlayerModel>
): MediaSource {
    if (models.isEmpty()) {
        throw IllegalArgumentException("PlayerModel 集合不能为kong")
    }
    stop(true)
    val source = if (models.count() == 1) {
        models[0].buildMediaSource(context)
    } else {
        ConcatenatingMediaSource(*models.map {
            it.buildMediaSource(context)
        }.toTypedArray())
    }
    prepare(source)
    return source
}

/**
 * 创建MediaSource
 */
internal fun SimpleExoPlayer.createSingleSource(
    context: Context,
    model: PlayerModel
): MediaSource {
    stop(true)
    val source = model.buildMediaSource(context)
    prepare(source)
    return source
}

/**
 * 创建exoPlayer播放器
 */
internal fun createSimpleExoPlayer(context: Context): SimpleExoPlayer {
    return SimpleExoPlayer
        .Builder(context)
        .build()
}

private fun PlayerModel.buildMediaSource(context: Context): MediaSource {
    return when (val type = Util.inferContentType(getMediaUri(), overrideExtension)) {
        C.TYPE_SS -> {
            SsMediaSource.Factory(
                DefaultHttpDataSourceFactory(
                    Util.getUserAgent(context, context.packageName)
                )
            ).createMediaSource(getMediaUri())
        }
        C.TYPE_DASH -> {
            DashMediaSource.Factory(
                DefaultHttpDataSourceFactory(
                    Util.getUserAgent(context, context.packageName)
                )
            ).createMediaSource(getMediaUri())
        }
        C.TYPE_HLS -> {
            HlsMediaSource.Factory(
                DefaultHttpDataSourceFactory(
                    Util.getUserAgent(context, context.packageName)
                )
            ).createMediaSource(getMediaUri())
        }
        C.TYPE_OTHER -> {
            ProgressiveMediaSource.Factory(
                DefaultDataSourceFactory(
                    context,
                    Util.getUserAgent(context, context.packageName)
                )
            ).createMediaSource(getMediaUri())
        }
        else -> {
            throw java.lang.IllegalArgumentException("Unsupported type: $type")
        }
    }
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