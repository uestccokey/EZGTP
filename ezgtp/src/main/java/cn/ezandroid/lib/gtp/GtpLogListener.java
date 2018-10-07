package cn.ezandroid.lib.gtp;

import android.util.Pair;

/**
 * Gtp日志监听器
 *
 * @author like
 * @date 2018-10-06
 */
public interface GtpLogListener {

    int TYPE_REQUEST = 1;
    int TYPE_RESPONSE = 2;
    int TYPE_MESSAGE = 3;

    void onGtpLogUpdated(GtpClient gtpClient, GtpLogQueue<Pair<String, Integer>> logQueue);
}
