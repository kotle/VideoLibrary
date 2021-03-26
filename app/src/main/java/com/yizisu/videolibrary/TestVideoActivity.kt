package com.yizisu.videolibrary

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.view.player_view.VideoPlayerView

class TestVideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_test_video)
        val view = findViewById<VideoPlayerView>(R.id.videoPlayer)
        val player = IYzsPlayer<PlayerModel>(this)
        view.attachPlayer(player)
        player.prepareAndPlay(mutableListOf(object : PlayerModel() {
            override fun callMediaUri(uriCall: (Uri?, Throwable?, Boolean) -> Unit) {
                uriCall.invoke(
                    Uri.parse("https://v-cdn.zjol.com.cn/276984.mp4"),
                    null,
                    false
                )
            }

            override fun getTitle(): CharSequence? {
                return "测试视频啊啊"
            }
        }))
    }
}