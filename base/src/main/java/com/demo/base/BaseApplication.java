package com.demo.base;

import android.app.Application;
import android.content.Context;

import com.demo.bridge.ApplicationLifecycleOwner;
import com.demo.bridge.BridgeManager;
import com.demo.bridge.ModuleApplicationManager;

import androidx.lifecycle.ViewModelProvider;

/**
 * Created by guoxiaodong on 2020/9/9 10:02
 */
public abstract class BaseApplication extends Application {
    private static BaseApplication INSTANCE;
    public ViewModelProvider.AndroidViewModelFactory viewModelFactory;

    public static BaseApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        BridgeManager.init(this);
        ModuleApplicationManager.init(this);
        viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this);
        ApplicationLifecycleOwner.INSTANCE.notifyOnCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ApplicationLifecycleOwner.INSTANCE.notifyOnAttachBaseContext(base);
    }
}