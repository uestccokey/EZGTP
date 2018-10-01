package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;

/**
 * Gtp客户端
 *
 * @author like
 * @date 2018-10-01
 */
public class GtpClient {

    private GtpEngine mBlackEngine;
    private GtpEngine mWhiteEngine;

    private volatile boolean mIsRunning;
    private volatile boolean mIsPause;

    private GtpListener mGtpListener;

    public GtpClient(GtpEngine blackEngine, GtpEngine whiteEngine) {
        mBlackEngine = blackEngine;
        mWhiteEngine = whiteEngine;
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
                boolean bConnected = mBlackEngine.connect();
                if (mGtpListener != null) {
                    mGtpListener.onStart(bConnected, true);
                }
                boolean wConnected = mWhiteEngine.connect();
                if (mGtpListener != null) {
                    mGtpListener.onStart(wConnected, false);
                }
                Point bMove = null;
                Point wMove = null;
                while (mIsRunning) {
                    if (!mIsPause) {
                        if (wMove != null) {
                            mBlackEngine.playMove(wMove, false);
                            if (mGtpListener != null) {
                                mGtpListener.onPlayMove(wMove, false);
                            }
                        }
                        if (!isResign(wMove)) {
                            bMove = mBlackEngine.genMove(true);
                            if (mGtpListener != null) {
                                mGtpListener.onGenMove(bMove, true);
                            }
                        }
                        if (bMove != null) {
                            mWhiteEngine.playMove(bMove, true);
                            if (mGtpListener != null) {
                                mGtpListener.onPlayMove(bMove, true);
                            }
                        }
                        if (!isResign(bMove)) {
                            wMove = mWhiteEngine.genMove(false);
                            if (mGtpListener != null) {
                                mGtpListener.onGenMove(wMove, false);
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
        mIsRunning = true;
        mIsPause = false;
    }

    public void resume() {
        if (!mIsPause || !mIsRunning) {
            return;
        }
        mIsPause = false;

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
        mIsPause = true;
        mIsRunning = false;

        mBlackEngine.disconnect();
        if (mGtpListener != null) {
            mGtpListener.onStop(true);
        }
        mWhiteEngine.disconnect();
        if (mGtpListener != null) {
            mGtpListener.onStop(false);
        }
    }
}
