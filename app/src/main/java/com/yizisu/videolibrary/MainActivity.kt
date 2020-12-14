package com.yizisu.videolibrary

import android.content.res.Configuration
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.PlayerFactory
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.service.PlayerServiceHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    companion object{
        val ssss=PlayerServiceHelper(LlPlayerService::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        ssss.getService(applicationContext){
//            println("333333333333")
//        }
        PlayerServiceHelper(LlPlayerService::class.java).getPlayer(this) {
            println("666666666666666666666")
        }
        onClick()
    }

    //    private val simplePlayer by lazy { createLifecycleSimplePlayer(this) }
    private lateinit var simplePlayer: IYzsPlayer<Mp4PlayerModel>
    private var videoHeight = 0
    private var videoWidth = 0
    private val point = Point()
    private fun onClick() {
        simplePlayer = PlayerFactory.createPlayer(this, PlayerFactory.PLAYER_IMPL_EXO)
        window.windowManager.defaultDisplay.getRealSize(point)
        simplePlayer.attachView(playerView)
        simplePlayer.addPlayerListener(object : SimplePlayerListener<Mp4PlayerModel> {
            override fun onTick(playerModel: Mp4PlayerModel) {
                progressTv.text = "当前进度：${playerModel.currentDurationText}\n" +
                        "缓存进度：${playerModel.currentBufferDurationText}\n" +
                        "总进度：${playerModel.totalDurationText}"
            }

            override fun onVideoSizeChange(
                width: Int,
                height: Int,
                unappliedRotationDegrees: Int,
                pixelWidthHeightRatio: Float,
                playerModel: Mp4PlayerModel?
            ) {
                if (videoWidth != width || videoHeight != height) {
                    videoHeight = height
                    videoWidth = width
                    initVideoViewSize(playerView.width, playerView)
                }
            }
        })
        play.setOnClickListener {
            simplePlayer = PlayerFactory.createPlayer(this, PlayerFactory.PLAYER_IMPL_EXO)
            simplePlayer.setAudioForceEnable(true)
            simplePlayer.prepareAndPlay(
                mutableListOf(
                    Mp4PlayerModel("http://168.168.168.168:8081/IXC543579cb3284e4d00fb01c1eda867faa/d8ed51430f814a384abb0fc56c876035/5f1d8006/video/tos/cn/tos-cn-ve-15/6c236887236d4b1c8d40eb84456818dd/?a=1128&br=3231&bt=1077&cr=0&cs=0&dr=0&ds=3&er=&l=20200726200641010011039233241BA0EC&lr=aweme_search_suffix&mime_type=video_mp4&qs=0&rc=M247djg0Nzo1djMzZWkzM0ApaTU7OzQ7aDw3Nzs5aDg0PGdfZHJlXi9qZGJfLS0xLS9zc19hYzVhYC9gMzAvNjVhLTU6Yw%3D%3D&vl=&vr="),
//                    Mp4PlayerModel("rtmp://184.72.239.149/vod/BigBuckBunny_115k.mov"),
                    Mp4PlayerModel("http://ivi.bupt.edu.cn/hls/cctv3hd.m3u8"),
                    Mp4PlayerModel("http://ivi.bupt.edu.cn/hls/cctv8hd.m3u8"),
                    Mp4PlayerModel("rtmp://202.69.69.180:443/webcast/bshdlive-pc"),
                    Mp4PlayerModel("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov")
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

    private fun initVideoViewSize(oldViewWidth: Int, view: View) {
        if (oldViewWidth > 0 && videoWidth > 0) {
            view.layoutParams = FrameLayout.LayoutParams(
                oldViewWidth, oldViewWidth * videoHeight / videoWidth
            ).apply {
                gravity = Gravity.CENTER
            }
        }
    }

    class Mp4PlayerModel(private val url: String) : PlayerModel() {
        override fun callMediaUri(uriCall: (Uri?, Throwable?, Boolean) -> Unit) {
            uriCall.invoke(Uri.parse(url), null, false)
        }

//            return "http://vjs.zencdn.net/v/oceans.mp4"
//            return Uri.parse("http://html5videoformatconverter.com/data/images/happyfit2.mp4")
//            return "https://media.w3.org/2010/05/sintel/trailer.mp4"
//            return "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)
        val videoView = findViewById<TextureView>(R.id.playerView)
        simplePlayer.attachView(videoView)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportActionBar?.hide()
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            initVideoViewSize(point.y, videoView)
        } else {
            supportActionBar?.show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            initVideoViewSize(point.x, videoView)
        }

    }
}
