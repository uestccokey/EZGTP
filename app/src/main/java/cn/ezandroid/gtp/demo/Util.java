package cn.ezandroid.gtp.demo;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Util
 *
 * @author like
 * @date 2018-04-15
 */
public class Util {

    public static void copyStream(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int len;
        while ((len = input.read(buf, 0, buf.length)) > 0) {
            output.write(buf, 0, len);
        }
    }

    public static void closeObject(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
