package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;

/**
 * Gtp监听器
 *
 * @author like
 * @date 2018-10-01
 */
public interface GtpListener {

    default void onStart(boolean isSuccess, boolean isBlack) {
    }

    default void onResume(boolean isBlack) {
    }

    default void onPlayMove(Point move, boolean isBlack) {
    }

    default void onGenMove(Point move, boolean isBlack) {
    }

    default void onPause(boolean isBlack) {
    }

    default void onStop(boolean isBlack) {
    }
}
