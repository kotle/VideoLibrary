package com.yizisu.playerlibrary.view;

import androidx.annotation.IntDef;

import com.yizisu.playerlibrary.PlayerFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Documented
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        PlayerFactory.LOOP_MODO_LIST,
        PlayerFactory.LOOP_MODO_NONE,
        PlayerFactory.LOOP_MODO_SHUFF,
        PlayerFactory.LOOP_MODO_SINGLE
})
public @interface SimplePlayerRepeatMode {
}
