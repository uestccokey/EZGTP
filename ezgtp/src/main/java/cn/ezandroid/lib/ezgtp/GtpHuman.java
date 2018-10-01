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
    private Point mWaitPlayMove;

    @Override
    public boolean connect(String... args) {
        return true;
    }

    @Override
    public String send(String command) {
        return "=";
    }

    @Override
    public void disconnect() {
    }

    public void setWaitPlayMove(Point playMove) {
        mWaitPlayMove = playMove;
        mLatch.countDown();
    }

    @Override
    public Point genMove(boolean isBlack) {
        try {
            mLatch = new CountDownLatch(1);
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mWaitPlayMove;
    }
}
