package cn.ezandroid.gtp.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.ezandroid.lib.ezgtp.GtpExecutableEngine;

/**
 * 里拉引擎
 *
 * @author like
 * @date 2018-06-06
 */
public class LeelaZeroEngine extends GtpExecutableEngine {

    private static final int EXE_VERSION = 180918;
    private static final String KEY_EXE_VERSION = "leelaz_version";

    private Context mContext;

    private File mContextDir;
    private File mEngineFile;
    private File mWeightFile;

    public LeelaZeroEngine(Context context) {
        mContext = context;
        mContextDir = mContext.getDir("engines", Context.MODE_PRIVATE);
        mWeightFile = new File(mContextDir, "weight.txt");
        mEngineFile = new File(mContextDir, "leelaz");
    }

    private File copyRawFile(File file, int id) {
        if (file.exists()) {
            file.delete();
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file), 4096);
            inputStream = new BufferedInputStream(mContext.getResources().openRawResource(id), 4096);

            Util.copyStream(inputStream, outputStream, 4096);

            new ProcessBuilder("chmod", "777", file.getAbsolutePath()).start().waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            Util.closeObject(inputStream);
            Util.closeObject(outputStream);
        }
        return file;
    }

    @Override
    public File getExecutableFile() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int version = prefs.getInt(KEY_EXE_VERSION, 0);
        if (version < EXE_VERSION) {
            copyRawFile(mEngineFile, R.raw.leelaz);
            copyRawFile(mWeightFile, R.raw.weight);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_EXE_VERSION, EXE_VERSION);
            editor.apply();
        }
        return mEngineFile;
    }

    @Override
    public boolean connect(String... args) {
        return super.connect("-g", // Gtp模式
                "-r", String.valueOf(10), // 低于10%的胜率认输
                "-w", mWeightFile.getAbsolutePath(), // 权重文件路径
                "-m", String.valueOf(10), // 前n手更随机
                "-b", String.valueOf(100), // 100厘秒
                "-t", String.valueOf(CPUUtil.getCPUCoresCount() - 1)); // 线程数
    }
}
