package com.yizisu.playerlibrary

import android.content.Context
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.yizisu.playerlibrary.helper.PlayerModel
import com.yizisu.playerlibrary.impl.BaseYzsPlayer
import com.yizisu.playerlibrary.impl.exoplayer.ExoPlayerImpl
import java.lang.IllegalArgumentException


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

    //创建播放器实例
    const val PLAYER_IMPL_EXO = 1

    /**
     * 创建一个播放器
     */
    fun <Model : PlayerModel> createPlayer(
        context: Context,
        @PlayerType playerImpl: Int
    ): IYzsPlayer<Model> {
        return when (playerImpl) {
            PLAYER_IMPL_EXO -> {
                ExoPlayerImpl(context)
            }
            else -> {
                throw IllegalArgumentException("无法创建播放器")
            }
        }
    }

    /**
     * 创建带有生命周期的Player
     */
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
    fun <Model : PlayerModel> createLifecyclePlayer(
        lifecycle: Fragment,
        playerImpl: Int
    ): IYzsPlayer<Model> {
        val player = createPlayer<Model>(lifecycle.context!!, playerImpl)
        lifecycle.lifecycle.addObserver(player)
        return player
    }
}
