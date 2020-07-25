package com.yizisu.playerlibrary

import android.content.Context
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.Player
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.impl.exoplayer.ExoPlayerImpl


/**
 * 一个简单的播放器
 */
object PlayerFactory {


    //单个循环
    const val LOOP_MODO_SINGLE = 1

    //全部循环
    const val LOOP_MODO_LIST = 2

    //不循环
    const val LOOP_MODO_NONE = 3

    //随机下一个
    const val LOOP_MODO_SHUFF = 4

    @MustBeDocumented
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(PlayerFactory.PLAYER_IMPL_EXO)
    annotation class PlayerType

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(REPEAT_MODE_OFF, REPEAT_MODE_ONE, REPEAT_MODE_ALL)
    annotation class RepeatMode

    /**
     * Normal playback without repetition.
     */
    const val REPEAT_MODE_OFF = Player.REPEAT_MODE_OFF

    /**
     * "Repeat One" mode to repeat the currently playing window infinitely.
     */
    const val REPEAT_MODE_ONE = Player.REPEAT_MODE_ONE

    /**
     * "Repeat All" mode to repeat the entire timeline infinitely.
     */
    const val REPEAT_MODE_ALL = Player.REPEAT_MODE_ALL

    //-----------------------------------------//
    //创建播放器实例
    const val PLAYER_IMPL_EXO = 1

    /**
     * 创建一个播放器
     */
    @Deprecated("使用接口的伴生对象方法创建")
    fun <Model : PlayerModel> createPlayer(
        context: Context,
        @PlayerType playerImpl: Int
    ): IYzsPlayer<Model> {
        return IYzsPlayer.invoke(context)
    }

    /**
     * 创建带有生命周期的Player
     */
    @Deprecated("使用接口的伴生对象方法创建")
    fun <Model : PlayerModel> createLifecyclePlayer(
        lifecycle: AppCompatActivity,
        playerImpl: Int
    ): IYzsPlayer<Model> {
        val player = createPlayer<Model>(lifecycle, playerImpl)
        lifecycle.lifecycle.addObserver(player)
        return player
    }

    /**
     * 创建带有生命周期的Player
     */
    @Deprecated("使用接口的伴生对象方法创建")
    fun <Model : PlayerModel> createLifecyclePlayer(
        lifecycle: Fragment,
        playerImpl: Int
    ): IYzsPlayer<Model> {
        val player = createPlayer<Model>(lifecycle.context!!, playerImpl)
        lifecycle.lifecycle.addObserver(player)
        return player
    }
}
