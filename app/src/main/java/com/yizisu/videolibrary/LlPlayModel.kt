package com.yizisu.videolibrary

import android.net.Uri
import com.yizisu.playerlibrary.helper.PlayerModel

class LlPlayModel(val bean: TvUrlBean) : PlayerModel() {
    override fun callMediaUri(uriCall: (Uri?, Throwable?, Boolean) -> Unit) {
        uriCall.invoke(Uri.parse(bean.url), null, false)
    }
}