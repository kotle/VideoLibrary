package com.yizisu.videolibrary

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.helper.createLifecycleSimplePlayer
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onClick()
    }

    private val simplePlayer by lazy { createLifecycleSimplePlayer(this) }

    private fun onClick() {
        simplePlayer.attachView(playerView)
        simplePlayer.addPlayerListener(object : SimplePlayerListener {
            override fun onBufferChange(playerModel: PlayerModel) {
                progressTv.text = "当前进度：${playerModel.currentDurationText}\n" +
                        "缓存进度：${playerModel.currentBufferDurationText}\n" +
                        "缓存百分比：${playerModel.bufferedPercentage}\n" +
                        "总进度：${playerModel.totalDurationText}"
            }
        })
        play.setOnClickListener {
            simplePlayer.prepareAndPlay(
                mutableListOf(
                    Mp4PlayerModel("http://html5videoformatconverter.com/data/images/happyfit2.mp4"),
//                    Mp4PlayerModel("rtmp://184.72.239.149/vod/BigBuckBunny_115k.mov"),
                    Mp4PlayerModel("http://ivi.bupt.edu.cn/hls/cctv3hd.m3u8"),
                    Mp4PlayerModel("http://ivi.bupt.edu.cn/hls/cctv8hd.m3u8"),
                    Mp4PlayerModel("rtmp://203.207.99.19:1935/live/CCTV7")
                )
            ) {

            }
        }
        play1.setOnClickListener {
            simplePlayer.play {

            }
        }
        pause.setOnClickListener {
            simplePlayer.pause {

            }
        }
        pre.setOnClickListener {
            simplePlayer.previous {

            }
        }
        next.setOnClickListener {
            simplePlayer.next {

            }
        }
        seek.setOnClickListener {
            simplePlayer.seekTo(0, 0) {

            }
        }
    }

    class Mp4PlayerModel(private val url: String) : PlayerModel() {
        override fun getMediaUri(): Uri {
            return Uri.parse(url)
//            return "http://vjs.zencdn.net/v/oceans.mp4"
//            return Uri.parse("http://html5videoformatconverter.com/data/images/happyfit2.mp4")
//            return "https://media.w3.org/2010/05/sintel/trailer.mp4"
//            return "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
        }
    }
}
