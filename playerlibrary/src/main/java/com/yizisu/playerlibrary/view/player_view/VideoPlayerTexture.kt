package com.yizisu.playerlibrary.view.player_view

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.helper.SimplePlayerListener
import com.yizisu.playerlibrary.view.RatioLayout
import com.yizisu.playerlibrary.view.autoBindListener

internal class VideoPlayerTexture : RatioLayout, SimplePlayerListener<PlayerModel> {
    private val textureView = TextureView(context)
    var player: IYzsPlayer<PlayerModel>? = null
        set(value) {
            field?.attachView(null)
            value?.attachView(textureView)
            autoBindListener(value, field, this)
            field = value
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        addView(textureView)
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
        setChildRatio(width.toFloat() / height)
    }
}