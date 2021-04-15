package com.yizisu.playerlibrary.view.player_view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.yizisu.playerlibrary.IYzsPlayer
import com.yizisu.playerlibrary.R
import com.yizisu.playerlibrary.helper.PlayerModel

internal class VideoPlayerTitleBar : LinearLayout {
    private val speedList = listOf(1f, 1.5f, 2f, 3f,4f)
    var player: IYzsPlayer<PlayerModel>? = null
        set(value) {
            field = value
            updateSpeed()
        }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    )

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    private val backIv: View
    private val titleTv: TextView
    private val speedTv: TextView

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.video_player_title_bar, this, true)
        backIv = root.findViewById(R.id.playerBack)
        titleTv = root.findViewById(R.id.playerTitleTv)
        speedTv = root.findViewById(R.id.speedTv)
        speedTv.setOnClickListener {
            selectSpeed()
        }
        backIv.setOnClickListener {
            (context as? Activity)?.onBackPressed()
        }
    }

    /**
     * 选择倍速弹窗
     */
    private fun selectSpeed() {
        AlertDialog.Builder(context, R.style.Theme_AppCompat_Dialog)
                .setTitle("选择倍速")
                .setPositiveButton(android.R.string.cancel, null)
                .setAdapter(
                        object : ArrayAdapter<String>(
                                context,
                                android.R.layout.simple_list_item_1,
                                speedList.map { "倍速$it" }
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                (view as TextView).setTextColor(Color.WHITE)
                                return view
                            }
                        }
                ) { dialog, which ->
                    player?.setVideoSpeed(speedList[which])
                    updateSpeed()
                }.show()
    }

    /**
     * 更新倍速文本
     */
    private fun updateSpeed() {
        player?.getVideoSpeed()?.let {
            speedTv.text = "倍速$it"
        }
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String?) {
        titleTv.text = title
    }
}