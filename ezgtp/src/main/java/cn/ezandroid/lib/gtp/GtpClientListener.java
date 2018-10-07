package cn.ezandroid.lib.gtp;

import android.graphics.Point;

/**
 * Gtp客户端监听器
 *
 * @author like
 * @date 2018-10-06
 */
public interface GtpClientListener {

    default void onPlayMove(Point move, boolean isBlack) {
    }

    default void onGenMove(Point move, boolean isBlack) {
    }
}
