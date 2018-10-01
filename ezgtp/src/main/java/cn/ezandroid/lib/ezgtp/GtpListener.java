package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;

/**
 * GtpListener
 *
 * @author like
 * @date 2018-10-01
 */
public interface GtpListener {

    void onConnected(boolean isSuccess, boolean isBlack);

    void onGenMove(Point move, boolean isBlack);
}
