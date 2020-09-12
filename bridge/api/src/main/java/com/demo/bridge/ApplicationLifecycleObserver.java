package com.demo.bridge;

import android.content.Context;

/**
 * Created by guoxiaodong on 2020/9/10 19:33
 */
public interface ApplicationLifecycleObserver {
    void onCreate();

    void attachBaseContext(Context base);
}
