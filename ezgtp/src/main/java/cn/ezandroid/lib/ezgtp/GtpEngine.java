package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;
import android.text.TextUtils;

/**
 * Gtp引擎
 *
 * @author like
 * @date 2018-10-01
 */
public abstract class GtpEngine {

    private static final String BLACK_NAME = "black";
    private static final String WHITE_NAME = "white";

    protected int mBoardSize = 19;
    protected float mKomi = 7.5f;

    /**
     * 连接引擎
     *
     * @return
     */
    public abstract boolean connect();

    /**
     * 发送命令
     *
     * @param command
     * @return
     */
    public abstract String send(String command);

    /**
     * 断开连接
     */
    public abstract void disconnect();

    protected boolean success(String response) {
        return !TextUtils.isEmpty(response) && response.charAt(0) == '=';
    }

    protected String color(boolean isBlack) {
        return isBlack ? BLACK_NAME : WHITE_NAME;
    }

    /**
     * 设置棋盘大小
     *
     * @param boardSize
     * @return
     */
    public String setBoardSize(int boardSize) {
        mBoardSize = boardSize;
        return send(GtpCommand.BOARD_SIZE.cmd(String.valueOf(boardSize)));
    }

    /**
     * 设置贴目
     *
     * @param komi
     * @return
     */
    public String setKomi(float komi) {
        mKomi = komi;
        return send(GtpCommand.KOMI.cmd(String.valueOf((int) (komi * 10.0f) / 10.0f)));
    }

    /**
     * 设置时限
     *
     * @param time
     * @return
     */
    public String timeSettings(int time) {
        return send(GtpCommand.TIME_SETTINGS.cmd("0", String.valueOf(time), "1"));
    }

    public String playMove(Point point, boolean isBlack) {
        return send(GtpCommand.PLAY.cmd(color(isBlack), GtpUtil.point2Coordinate(point, mBoardSize)));
    }

    public Point genMove(boolean isBlack) {
        return GtpUtil.coordinate2Point(send(GtpCommand.GEN_MOVE.cmd(color(isBlack))), mBoardSize);
    }

    /**
     * 悔棋
     *
     * @param doubleUndo
     * @return
     */
    public boolean undo(boolean doubleUndo) {
        if (success(send(GtpCommand.UNDO.cmd()))) {
            if (doubleUndo) {
                return success(send(GtpCommand.UNDO.cmd()));
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 点目
     *
     * @return
     */
    public String finalScore() {
        return send(GtpCommand.FINAL_SCORE.cmd()).split(" ")[1];
    }
}
