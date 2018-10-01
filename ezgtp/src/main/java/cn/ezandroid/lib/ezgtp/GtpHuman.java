package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;

import java.util.concurrent.CountDownLatch;

/**
 * GtpHuman
 *
 * @author like
 * @date 2018-10-01
 */
public class GtpHuman extends GtpEngine {

    private CountDownLatch mLatch;
    private boolean mIsBlack;
    private Point mWaitPlayMove;

    @Override
    public String playMove(Point point, boolean isBlack) {
        if (mIsBlack == isBlack) {
            mWaitPlayMove = point;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }
        return super.playMove(point, isBlack);
    }

    @Override
    public Point genMove(boolean isBlack) {
        mIsBlack = isBlack;
        try {
            mLatch = new CountDownLatch(1);
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mWaitPlayMove;
    }
}
