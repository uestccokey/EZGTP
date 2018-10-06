package cn.ezandroid.lib.ezgtp;

/**
 * Gtp监听器
 *
 * @author like
 * @date 2018-10-01
 */
public interface GtpGameListener {

    default void onStart(boolean isSuccess, boolean isBlack) {
    }

    default void onResume(boolean isBlack) {
    }

    default void onPause(boolean isBlack) {
    }

    default void onStop(boolean isBlack) {
    }
}
