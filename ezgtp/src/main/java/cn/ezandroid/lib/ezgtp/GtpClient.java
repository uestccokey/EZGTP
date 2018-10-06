package cn.ezandroid.lib.ezgtp;

import android.graphics.Point;
import android.text.TextUtils;
import android.util.Pair;

/**
 * Gtp客户端基类
 * <p>
 * 用于加载支持Gtp命令的可执行程序
 *
 * @author like
 * @date 2018-10-01
 */
public abstract class GtpClient {

    private static final String BLACK_NAME = "black";
    private static final String WHITE_NAME = "white";

    protected int mBoardSize = 19;
    protected float mKomi = 7.5f;

    private final GtpLogQueue<Pair<String, Integer>> mLogQueue = new GtpLogQueue<>(256);
    private GtpLogListener mLogListener;

    public void setGtpLogListener(GtpLogListener logListener) {
        mLogListener = logListener;
    }

    public GtpLogQueue<Pair<String, Integer>> getLogQueue() {
        return mLogQueue;
    }

    protected void addLog(Pair<String, Integer> log) {
        synchronized (mLogQueue) {
            mLogQueue.offer(log);
        }
    }

    protected void notifyLogUpdated() {
        if (mLogListener != null) {
            mLogListener.onGtpLogUpdated(this, mLogQueue);
        }
    }

    /**
     * 连接引擎（耗时操作，需要在异步线程执行）
     *
     * @return
     */
    public boolean connect(String... args) {
        return true;
    }

    /**
     * 发送命令
     *
     * @param command
     * @return
     */
    public String send(String command) {
        addLog(new Pair<>(command, GtpLogListener.TYPE_REQUEST));
        notifyLogUpdated();

        String response = "= ";
        addLog(new Pair<>(response, GtpLogListener.TYPE_RESPONSE));
        notifyLogUpdated();
        return response;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        mLogListener = null;
    }

    public static boolean success(String response) {
        return !TextUtils.isEmpty(response) && response.charAt(0) == '=';
    }

    public static String color(boolean isBlack) {
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

    /**
     * 落子
     *
     * @param point
     * @param isBlack
     * @return
     */
    public String playMove(Point point, boolean isBlack) {
        return send(GtpCommand.PLAY.cmd(color(isBlack), GtpUtil.point2Coordinate(point, mBoardSize)));
    }

    /**
     * 命令AI落子（耗时操作，需要在异步线程执行）
     *
     * @param isBlack
     * @return
     */
    public Point genMove(boolean isBlack) {
        String move = send(GtpCommand.GEN_MOVE.cmd(color(isBlack)));
        return GtpUtil.coordinate2Point(move.substring(move.indexOf(' ') + 1).trim(), mBoardSize);
    }

    /**
     * 显示棋盘
     *
     * @return
     */
    public String showBoard() {
        return send(GtpCommand.SHOW_BOARD.cmd());
    }

    /**
     * 清空棋盘
     *
     * @return
     */
    public String clearBoard() {
        return send(GtpCommand.CLEAR_BOARD.cmd());
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
