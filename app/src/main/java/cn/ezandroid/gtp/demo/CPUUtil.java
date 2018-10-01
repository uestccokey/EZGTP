package cn.ezandroid.gtp.demo;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * CPUUtil
 *
 * @author like
 * @date 2018-02-08
 */
public class CPUUtil {

    public static final String ARM64_V8A = "arm64-v8a";
    public static final String ARMEABI_V7A = "armeabi-v7a";
    public static final String ARMEABI = "armeabi";
    public static final String X86_64 = "x86_64";
    public static final String X86 = "X86";
    public static final String MIPS_64 = "mips_64";
    public static final String MIPS = "mips";

    public static boolean isARM64Support() {
        String abi = Build.CPU_ABI;
        String abi2 = Build.CPU_ABI2;
        return ARM64_V8A.equals(abi)
                || ARM64_V8A.equals(abi2);
    }

    public static boolean isARMSupport() {
        String abi = Build.CPU_ABI;
        String abi2 = Build.CPU_ABI2;
        return ARM64_V8A.equals(abi) || ARMEABI_V7A.equals(abi) || ARMEABI.equals(abi)
                || ARM64_V8A.equals(abi2) || ARMEABI_V7A.equals(abi2) || ARMEABI.equals(abi2);
    }

    public static boolean isX86Support() {
        String abi = Build.CPU_ABI;
        String abi2 = Build.CPU_ABI2;
        return X86_64.equals(abi) || X86.equals(abi)
                || X86_64.equals(abi2) || X86.equals(abi2);
    }

    public static boolean isMipsSupport() {
        String abi = Build.CPU_ABI;
        String abi2 = Build.CPU_ABI2;
        return MIPS_64.equals(abi) || MIPS.equals(abi)
                || MIPS_64.equals(abi2) || MIPS.equals(abi2);
    }

    /**
     * 获取CPU核数
     *
     * @return
     */
    public static int getCPUCoresCount() {
        //Private Class to display only CPU devices in the directory listing
        class CPUFilter implements FileFilter {
            @Override
            public boolean accept(File pathName) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]", pathName.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CPUFilter());
            Log.d("CPUUtil", "CPU Count: " + files.length);
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Print exception
            Log.d("CPUUtil", "CPU Count: Failed.");
            e.printStackTrace();
            //Default to return 1 core
            return 1;
        }
    }
}
