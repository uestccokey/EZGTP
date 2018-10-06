package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;
import android.util.Pair;

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
    private boolean mIsWaitingPlay;
    private Point mWaitingPlayMove = new Point(GtpUtil.PASS_POS, GtpUtil.PASS_POS);

    @Override
    public String playMove(Point point, boolean isBlack) {
        if (mIsWaitingPlay && mIsPlayBlack == isBlack) {
            mWaitingPlayMove = point;
            if (mLatch != null) {
                mLatch.countDown();
            }
            mIsWaitingPlay = false;
            return "= ";
        } else {
            return super.playMove(point, isBlack);
        }
    }

    @Override
    public Point genMove(boolean isBlack) {
        addLog(new Pair<>(GtpCommand.GEN_MOVE.cmd(color(isBlack)), GtpLogListener.TYPE_REQUEST));
        notifyLogUpdated();

        mIsWaitingPlay = true;
        mIsPlayBlack = isBlack;
        try {
            mLatch = new CountDownLatch(1);
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String response = "= " + GtpUtil.point2Coordinate(mWaitingPlayMove, mBoardSize);
        addLog(new Pair<>(response, GtpLogListener.TYPE_RESPONSE));
        notifyLogUpdated();
        return mWaitingPlayMove;
    }

    @Override
    public void disconnect() {
        super.disconnect();
        if (mLatch != null) {
            mLatch.countDown();
        }
        mIsWaitingPlay = false;
    }
}
