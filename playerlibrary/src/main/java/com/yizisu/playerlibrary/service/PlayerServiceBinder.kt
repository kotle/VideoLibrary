package com.yizisu.playerlibrary.service

import android.os.Binder

internal class PlayerServiceBinder(val service: BasePlayerService<*>):Binder() {
}