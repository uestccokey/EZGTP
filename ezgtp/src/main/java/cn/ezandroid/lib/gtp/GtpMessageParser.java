package cn.ezandroid.lib.gtp;

/**
 * Gtp消息解析器
 *
 * @author like
 * @date 2018-10-06
 */
public interface GtpMessageParser {

    boolean parse(String line);
}
