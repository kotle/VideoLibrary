package com.yizisu.playerlibrary.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.helper.fullScreen
import com.yizisu.playerlibrary.view.SimplePlayerView
import com.yizisu.playerlibrary.view.player_view.VideoPlayerView
import java.io.Serializable

class FullScreenVideoActivity : AppCompatActivity(), SimplePlayerListener<PlayerModel> {
    companion object {
        fun start(appCompatActivity: AppCompatActivity?, info: VideoInfo) {
            appCompatActivity ?: return
            val oldScreen = appCompatActivity.requestedOrientation
            val intent = Intent(appCompatActivity, FullScreenVideoActivity::class.java)
            intent.putExtra("orientation", oldScreen)
            intent.putExtra("info", info)
            ActivityCompat.startActivity(appCompatActivity, intent, null)
        }

        fun start(appCompatActivity: AppCompatActivity?, url: String) {
            start(appCompatActivity, VideoInfo(0, 0, null, url))
        }

        fun start(appCompatActivity: AppCompatActivity?, url: String, title: String?) {
            start(appCompatActivity, VideoInfo(0, 0, title, url))
        }
    }

    data class VideoInfo(
            val width: Int,
            val height: Int,
            val title: String?,
            val url: String?
    ) : Serializable

    private val orientation by lazy {
        intent.getIntExtra(
                "orientation",
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
        )
    }
    private val info by lazy { intent.getSerializableExtra("info") as? VideoInfo }

    override fun onCreate(savedInstanceState: Bundle?) {
        fullScreen(true)
        var videoData: VideoInfo? = null
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                val data = intent?.dataString
                if (data.isNullOrBlank()) {
                    finishAfterTransition()
                    Toast.makeText(this, "没有可播放视频", Toast.LENGTH_LONG).show()
                    return
                } else {
                    videoData = VideoInfo(
                            0,
                            0,
                            intent.getStringExtra(Intent.EXTRA_TITLE)
                                    ?: intent.extras?.getString(Intent.EXTRA_TITLE),
                            data
                    )
                }
            }
            else -> {
                videoData = info
            }
        }
        if (videoData?.url == null) {
            finishAfterTransition()
            Toast.makeText(this, "播放地址未null", Toast.LENGTH_LONG).show()
            return
        }
        setOrientation(videoData)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_video)
        player.addPlayerListener(this)
        val playerView = findViewById<VideoPlayerView>(R.id.playerView)
        playerView.attachPlayer(player)
        player.prepareAndPlay(mutableListOf(object : PlayerModel() {
            override fun callMediaUri(uriCall: (Uri?, Throwable?, Boolean) -> Unit) {
                uriCall.invoke(Uri.parse(videoData.url), null, false)
            }

            override fun getTitle(): CharSequence? {
                return videoData.title
            }
        }))
        playerView.isFullScreenData.value = true
        //隐藏全屏按钮
        findViewById<View>(R.id.ivFull).visibility = View.GONE
        findViewById<View>(R.id.playerBack).setOnClickListener {
            finishAfterTransition()
        }
    }

    /**
     * 根据宽高设置视频方向
     */
    private fun setOrientation(info: VideoInfo?) {
        info ?: return
        val width = info.width
        val height = info.height
        try {
            requestedOrientation = if (width <= 0 || height <= 0) {
                orientation
            } else {
                if (width > height) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private val player by lazy {
        IYzsPlayer<PlayerModel>(this).apply {
            lifecycle.addObserver(this)
        }
    }

    override fun onTick(playerModel: PlayerModel) {

    }

    override fun onVideoSizeChange(
            width: Int,
            height: Int,
            unappliedRotationDegrees: Int,
            pixelWidthHeightRatio: Float,
            playerModel: PlayerModel?
    ) {
        super.onVideoSizeChange(
                width,
                height,
                unappliedRotationDegrees,
                pixelWidthHeightRatio,
                playerModel
        )
        requestedOrientation =
                if (width >= height) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        try {
            super.setRequestedOrientation(requestedOrientation)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}