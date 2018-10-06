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

    private GtpGameListener mGtpGameListener;

    private boolean mIsBlackFirstPlay; // 一般黑棋先走，让子时则白棋先走

    private final Object mPauseLock = new Object();

    public GtpGame(GtpClient blackClient, GtpClient whiteClient, boolean isBlackFirstPlay) {
        mBlackClient = blackClient;
        mWhiteClient = whiteClient;
        mIsBlackFirstPlay = isBlackFirstPlay;
    }

    public void setGtpGameListener(GtpGameListener listener) {
        mGtpGameListener = listener;
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
        mIsRunning = true;
        mIsPause = false;
        checkUnlock();

        new Thread() {
            @Override
            public void run() {
                boolean bConnected = mBlackClient.connect();
                if (mGtpGameListener != null) {
                    if (bConnected) {
                        mGtpGameListener.onStart(true);
                    } else {
                        GtpGame.this.stop();

                        mGtpGameListener.onFail(true);
                        return;
                    }
                }
                boolean wConnected = mWhiteClient.connect();
                if (mGtpGameListener != null) {
                    if (wConnected) {
                        mGtpGameListener.onStart(false);
                    } else {
                        GtpGame.this.stop();

                        mGtpGameListener.onFail(false);
                        return;
                    }
                }

                Point bMove = null;
                Point wMove = null;
                while (mIsRunning) {
                    if (mIsBlackFirstPlay) {
                        bMove = doBlackClient(wMove);
                        wMove = doWhiteClient(bMove);
                    } else {
                        wMove = doWhiteClient(bMove);
                        bMove = doBlackClient(wMove);
                    }
                }
            }
        }.start();
    }

    private Point doBlackClient(Point wMove) {
        checkLock();

        if (wMove != null) {
            mBlackClient.playMove(wMove, false);

            checkLock();

            mBlackClient.onPlayMove(wMove, false);
        }

        checkLock();

        Point bMove = null;
        if (!isResign(wMove)) {
            bMove = mBlackClient.genMove(true);

            checkLock();

            mBlackClient.onGenMove(bMove, true);
        }
        return bMove;
    }

    private Point doWhiteClient(Point bMove) {
        checkLock();

        if (bMove != null) {
            mWhiteClient.playMove(bMove, true);

            checkLock();

            mWhiteClient.onPlayMove(bMove, true);
        }

        checkLock();

        Point wMove = null;
        if (!isResign(bMove)) {
            wMove = mWhiteClient.genMove(false);

            checkLock();

            mWhiteClient.onGenMove(wMove, false);
        }
        return wMove;
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

        if (mGtpGameListener != null) {
            mGtpGameListener.onResume(true);
        }
        if (mGtpGameListener != null) {
            mGtpGameListener.onResume(false);
        }
    }

    public void pause() {
        if (mIsPause || !mIsRunning) {
            return;
        }
        mIsPause = true;

        if (mGtpGameListener != null) {
            mGtpGameListener.onPause(true);
        }
        if (mGtpGameListener != null) {
            mGtpGameListener.onPause(false);
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
        if (mGtpGameListener != null) {
            mGtpGameListener.onStop(true);
        }
        mWhiteClient.disconnect();
        if (mGtpGameListener != null) {
            mGtpGameListener.onStop(false);
        }
    }
}
