package com.yizisu.playerlibrary.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.view.SimplePlayerView
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
        var videoData: VideoInfo? = null
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                val data = intent?.dataString
                if (data.isNullOrBlank()) {
                    onBackPressed()
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
            onBackPressed()
            Toast.makeText(this, "没有可播放视频", Toast.LENGTH_LONG).show()
            return
        }
        setOrientation(videoData)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_video)
        player.addPlayerListener(this)
        val playerView = findViewById<SimplePlayerView>(R.id.playerView)
        playerView.attachPlayer(player)
        player.prepareAndPlay(mutableListOf(object : PlayerModel() {
            override fun callMediaUri(uriCall: (Uri?, Throwable?, Boolean) -> Unit) {
                uriCall.invoke(Uri.parse(videoData.url), null, false)
            }
        }))
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
            if (width >= height && requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
    }
}