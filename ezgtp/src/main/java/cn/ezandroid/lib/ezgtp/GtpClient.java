package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;

/**
 * GtpClient
 *
 * @author like
 * @date 2018-10-01
 */
public class GtpClient {

    private GtpEngine mBlackEngine;
    private GtpEngine mWhiteEngine;

    private Thread mPlayThread;
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

    private boolean isResign(Point point) {
        return point != null && point.x == GtpUtil.RESIGN_POS;
    }

    public void start() {
        if (mPlayThread == null) {
            mPlayThread = new Thread() {
                @Override
                public void run() {
                    boolean bConnected = mBlackEngine.connect();
                    if (bConnected) {
                        mBlackEngine.setBoardSize(19);
                        mBlackEngine.setKomi(7.5f);
                        mBlackEngine.timeSettings(5);
                    }
                    if (mGtpListener != null) {
                        mGtpListener.onConnected(bConnected, true);
                    }
                    boolean wConnected = mWhiteEngine.connect();
                    if (wConnected) {
                        mWhiteEngine.setBoardSize(19);
                        mWhiteEngine.setKomi(7.5f);
                        mWhiteEngine.timeSettings(5);
                    }
                    if (mGtpListener != null) {
                        mGtpListener.onConnected(wConnected, false);
                    }
                    Point bMove = null;
                    Point wMove = null;
                    while (mIsRunning) {
                        if (!mIsPause) {
                            if (wMove != null) {
                                mBlackEngine.playMove(wMove, false);
                            }
                            if (!isResign(wMove)) {
                                bMove = mBlackEngine.genMove(true);
                                if (mGtpListener != null) {
                                    mGtpListener.onGenMove(bMove, true);
                                }
                            }
                            if (bMove != null) {
                                mWhiteEngine.playMove(bMove, true);
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
            };
            mPlayThread.start();
            mIsRunning = true;
            mIsPause = false;
        }
    }

    public void resume() {
        mIsPause = false;
    }

    public void pause() {
        mIsPause = true;
    }

    public void stop() {
        mIsPause = true;
        mIsRunning = false;
    }
}
