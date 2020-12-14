package com.yizisu.playerlibrary.service

import android.os.Binder

class PlayerServiceBinder(val service: BasePlayerService<*>):Binder() {
}