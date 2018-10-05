package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;

import java.util.concurrent.CountDownLatch;

/**
 * 用于将人类落子转换为Gtp命令
 *
 * @author like
 * @date 2018-10-01
 */
public class GtpHuman extends GtpClient {

    private CountDownLatch mLatch;
    private boolean mIsPlayBlack;
    private Point mWaitPlayMove;

    @Override
    public String playMove(Point point, boolean isBlack) {
        if (mIsPlayBlack == isBlack) {
            mWaitPlayMove = point;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }
        return super.playMove(point, isBlack);
    }

    @Override
    public Point genMove(boolean isBlack) {
        mIsPlayBlack = isBlack;
        try {
            mLatch = new CountDownLatch(1);
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mWaitPlayMove;
    }
}
