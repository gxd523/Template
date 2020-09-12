package com.demo.bridge;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dalvik.system.DexFile;

public class ClassUtil {
    /**
     * 获得程序所有的apk(instant run会产生很多split apk)
     */
    private static List<String> getApkPathList(Context context) throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        List<String> sourcePathList = new ArrayList<>();
        sourcePathList.add(applicationInfo.sourceDir);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] splitSourceDirs = applicationInfo.splitSourceDirs;
            if (null != splitSourceDirs) {// instant run
                sourcePathList.addAll(Arrays.asList(splitSourceDirs));
            }
        }
        return sourcePathList;
    }

    public static Set<String> getGenerateBridgeClassName(Application context, final String packageName) throws Exception {
        final Set<String> classNameSet = new HashSet<>();
        List<String> apkPathList = getApkPathList(context);
        // 使用同步计数器判断均处理完成
        final CountDownLatch countDownLatch = new CountDownLatch(apkPathList.size());
        ExecutorService executorService = Executors.newFixedThreadPool(apkPathList.size());
        for (final String apkPath : apkPathList) {
            executorService.execute(() -> {
                DexFile dexfile = null;
                try {
                    // TODO: 2020/4/6 加载apk中的dex并遍历,获得所有包名为{packageName}的类
                    dexfile = new DexFile(apkPath);
                    Enumeration<String> dexEntries = dexfile.entries();
                    while (dexEntries.hasMoreElements()) {
                        String className = dexEntries.nextElement();
                        if (className.startsWith(packageName)) {
                            classNameSet.add(className);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != dexfile) {
                        try {
                            dexfile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //释放1个
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();// 等待执行完成
        return classNameSet;
    }
}
