package cn.ezandroid.lib.gtp;

import android.graphics.Point;

/**
 * Gtp工具类
 *
 * @author like
 * @date 2018-10-01
 */
public class GtpUtil {

    public static final int PASS_POS = -1;
    public static final int RESIGN_POS = -3;

    private static final String BOARD_LETTERS = "ABCDEFGHJKLMNOPQRSTUVWXYZ"; // 'I' is missing

    public static String point2Coordinate(Point point, int boardSize) {
        if (point.x == PASS_POS) {
            return "pass";
        } else if (point.x == RESIGN_POS) {
            return "resign";
        } else {
            return String.valueOf(BOARD_LETTERS.charAt(point.x)) + (boardSize - point.y);
        }
    }

    public static Point coordinate2Point(String coords, int boardSize) {
        if (coords.equalsIgnoreCase("pass")) {
            return new Point(PASS_POS, PASS_POS);
        } else if (coords.equalsIgnoreCase("resign")) {
            return new Point(RESIGN_POS, RESIGN_POS);
        } else {
            try {
                return new Point(BOARD_LETTERS.indexOf(coords.charAt(0)), boardSize - Integer.parseInt(coords.substring(1).trim()));
            } catch (Exception e) {
                e.printStackTrace();
                return new Point(PASS_POS, PASS_POS);
            }
        }
    }
}
