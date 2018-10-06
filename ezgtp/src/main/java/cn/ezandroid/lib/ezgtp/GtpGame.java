package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;

/**
 * Gtp游戏
 * <p>
 * 用于使两个Gtp客户端自动进行游戏
 *
 * @author like
 * @date 2018-10-01
 */
public class GtpGame {

    private GtpClient mBlackClient;
    private GtpClient mWhiteClient;

    private volatile boolean mIsRunning;
    private volatile boolean mIsPause;

    private GtpListener mGtpListener;

    private final Object mPauseLock = new Object();

    public GtpGame(GtpClient blackClient, GtpClient whiteClient) {
        mBlackClient = blackClient;
        mWhiteClient = whiteClient;
    }

    public void setGtpListener(GtpListener listener) {
        mGtpListener = listener;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public boolean isPause() {
        return mIsPause;
    }

    private boolean isResign(Point point) {
        return point != null && point.x == GtpUtil.RESIGN_POS;
    }

    public void start() {
        if (mIsRunning) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                boolean bConnected = mBlackClient.connect();
                if (mGtpListener != null) {
                    mGtpListener.onStart(bConnected, true);
                }
                boolean wConnected = mWhiteClient.connect();
                if (mGtpListener != null) {
                    mGtpListener.onStart(wConnected, false);
                }
                Point bMove = null;
                Point wMove = null;
                while (mIsRunning) {
                    checkLock();

                    if (wMove != null) {
                        mBlackClient.playMove(wMove, false);
                        if (mGtpListener != null) {
                            mGtpListener.onPlayMove(wMove, false);
                        }
                    }
                    if (!isResign(wMove)) {
                        bMove = mBlackClient.genMove(true);

                        checkLock();

                        if (mGtpListener != null) {
                            mGtpListener.onGenMove(bMove, true);
                        }
                    }

                    checkLock();

                    if (bMove != null) {
                        mWhiteClient.playMove(bMove, true);
                        if (mGtpListener != null) {
                            mGtpListener.onPlayMove(bMove, true);
                        }
                    }
                    if (!isResign(bMove)) {
                        wMove = mWhiteClient.genMove(false);

                        checkLock();

                        if (mGtpListener != null) {
                            mGtpListener.onGenMove(wMove, false);
                        }
                    }
                }
            }
        }.start();
        mIsRunning = true;
        mIsPause = false;
        checkUnlock();
    }

    private void checkLock() {
        if (mIsPause) {
            synchronized (mPauseLock) {
                try {
                    mPauseLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkUnlock() {
        if (!mIsPause) {
            synchronized (mPauseLock) {
                mPauseLock.notify();
            }
        }
    }

    public void resume() {
        if (!mIsPause || !mIsRunning) {
            return;
        }
        mIsPause = false;
        checkUnlock();

        if (mGtpListener != null) {
            mGtpListener.onResume(true);
        }
        if (mGtpListener != null) {
            mGtpListener.onResume(false);
        }
    }

    public void pause() {
        if (mIsPause || !mIsRunning) {
            return;
        }
        mIsPause = true;

        if (mGtpListener != null) {
            mGtpListener.onPause(true);
        }
        if (mGtpListener != null) {
            mGtpListener.onPause(false);
        }
    }

    public void stop() {
        if (!mIsRunning) {
            return;
        }
        mIsRunning = false;
        mIsPause = false;
        checkUnlock();

        mBlackClient.disconnect();
        if (mGtpListener != null) {
            mGtpListener.onStop(true);
        }
        mWhiteClient.disconnect();
        if (mGtpListener != null) {
            mGtpListener.onStop(false);
        }
    }
}
