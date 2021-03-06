package com.yizisu.playerlibrary.impl

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.media.AudioAttributesCompat

class AudioFocusHelper(
    context: Context,
    private val audioFocusListener: AudioManager.OnAudioFocusChangeListener
) {
    //如果需要新版的api ，这里设置为false
    //新版本的api ，自己断开焦点，别的app收不到回调
    private val onlyUserOldFun = true
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioAttributes = AudioAttributesCompat.Builder()
        .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
        .setUsage(AudioAttributesCompat.USAGE_MEDIA)
        .build()

    /*  private val audioFocusListener =
          AudioManager.OnAudioFocusChangeListener { focusChange ->
              when (focusChange) {
                  AudioManager.AUDIOFOCUS_GAIN -> {
                      if (shouldPlayWhenReady || player.playWhenReady) {
                          player.playWhenReady = true
                      }
                      shouldPlayWhenReady = false
                  }
                  AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                      if (player.playWhenReady) {
                      }
                  }
                  AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                      shouldPlayWhenReady = player.playWhenReady
                      player.playWhenReady = false
                  }
                  AudioManager.AUDIOFOCUS_LOSS -> {
                      abandonAudioFocus()
                  }
              }
          }*/

    @get:RequiresApi(Build.VERSION_CODES.O)
    private val audioFocusRequest by lazy { buildFocusRequest() }


    fun requestAudioFocus() {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !onlyUserOldFun) {
            requestAudioFocusOreo()
        } else {
            @Suppress("deprecation")
            audioManager.requestAudioFocus(
                audioFocusListener,
                audioAttributes.legacyStreamType,
                //请求焦点类型
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioFocusListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        } else {
            audioFocusListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS)
        }
    }

    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !onlyUserOldFun) {
            abandonAudioFocusOreo()
        } else {
            @Suppress("deprecation")
            audioManager.abandonAudioFocus(audioFocusListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusOreo(): Int =
        audioManager.requestAudioFocus(audioFocusRequest)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun abandonAudioFocusOreo() =
        audioManager.abandonAudioFocusRequest(audioFocusRequest)

    @TargetApi(Build.VERSION_CODES.O)
    private fun buildFocusRequest(): AudioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                audioAttributes.unwrap() as AudioAttributes
            )
            .setOnAudioFocusChangeListener(audioFocusListener)
            .build()
}