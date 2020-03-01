package com.yizisu.playerlibrary.helper

interface SimplePlayerListener {
    //缓存进度，每秒钟回调一次
    fun onBufferChange(playerModel: PlayerModel)
}