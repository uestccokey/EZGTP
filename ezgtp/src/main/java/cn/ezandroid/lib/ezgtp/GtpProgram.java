package cn.ezandroid.lib.ezgtp;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

/**
 * Gtp程序基类
 *
 * @author like
 * @date 2018-10-01
 */
public abstract class GtpProgram extends GtpClient {

    private static final String TAG = "GtpProgram";

    private Process mProcess;

    private Thread mMessageThread;
    private Thread mWaitThread;
    private OutputStreamWriter mWriter;
    private BufferedReader mReader;

    public abstract File getExecutableFile();

    @Override
    public boolean connect(String... args) {
        try {
            int len = args.length;
            String[] processArgs = new String[len + 1];
            processArgs[0] = getExecutableFile().getAbsolutePath();
            System.arraycopy(args, 0, processArgs, 1, len);
            mProcess = new ProcessBuilder(processArgs).start();

            Log.e(TAG, "Program Connect Success:" + Arrays.toString(processArgs));
        } catch (IOException e) {
            e.printStackTrace();

            Log.e(TAG, "Program Connect Fail:" + Arrays.toString(args));
            return false;
        }

        InputStream is = mProcess.getInputStream();
        mReader = new BufferedReader(new InputStreamReader(is), 8192);
        mWriter = new OutputStreamWriter(mProcess.getOutputStream());

        mMessageThread = new Thread(() -> {
            InputStream is1 = mProcess.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is1), 8192);
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.e(TAG, ": " + line);
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                }
            } catch (IOException ignored) {
            }
        });
        mMessageThread.start();

        mWaitThread = new Thread(() -> {
            try {
                Process ep = mProcess;
                if (ep != null) {
                    ep.waitFor();
                }
                Log.w(TAG, "Program Exit!");
            } catch (InterruptedException ignored) {
            }
        });
        mWaitThread.start();
        Log.w(TAG, "Program Start!");
        return true;
    }

    @Override
    public String send(String command) {
        try {
            Log.e(TAG, "Program Send: " + command);
            mWriter.write(command + "\n");
            mWriter.flush();
            addLog(new Pair<>(command, GtpLogListener.TYPE_REQUEST));
            notifyLogUpdated();
            String res;
            char ch;
            do {
                res = mReader.readLine();
                if (res == null) {
                    throw new IOException("Program Exception!");
                }
                Log.d(TAG, ": " + res);
                addLog(new Pair<>(res, GtpLogListener.TYPE_RESPONSE));
                notifyLogUpdated();
                ch = res.length() > 0 ? res.charAt(0) : 0;
            } while (ch != '=' && ch != '?');
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "? ";
    }

    @Override
    public void disconnect() {
        super.disconnect();
        if (mProcess != null) {
            mProcess.destroy();
        }
        if (mReader != null) {
            try {
                mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mWriter != null) {
            try {
                mWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e(TAG, "Program Disconnect");
    }
}
