package com.demo.module;

import android.content.Context;
import android.util.Log;

import com.demo.annotation.ModuleSpec;
import com.demo.bridge.ApplicationLifecycleObserver;

@ModuleSpec
public class ModuleApplication implements ApplicationLifecycleObserver {
    @Override
    public void onCreate() {
        Log.d("gxd", "ModuleApplication.onCreate-->");
    }

    @Override
    public void attachBaseContext(Context base) {
    }
}
