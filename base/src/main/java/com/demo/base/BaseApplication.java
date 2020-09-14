package com.demo.base;

import android.app.Application;
import android.content.Context;

import com.demo.bridge.ApplicationLifecycleOwner;
import com.demo.bridge.BridgeManager;
import com.demo.bridge.ClassUtil;
import com.demo.bridge.ModuleApplicationManager;

import java.util.Set;

import androidx.lifecycle.ViewModelProvider;

/**
 * Created by guoxiaodong on 2020/9/9 10:02
 */
public abstract class BaseApplication extends Application {
    public static ViewModelProvider.AndroidViewModelFactory viewModelFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        TaskManager.INSTANCE.addTask(() -> {
            try {
                Set<String> generateClassSet = ClassUtil.getGenerateClassSet(BaseApplication.this, "com.demo.generate");
                BridgeManager.init(generateClassSet);
                ModuleApplicationManager.init(generateClassSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this);
        ApplicationLifecycleOwner.INSTANCE.notifyOnCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ApplicationLifecycleOwner.INSTANCE.notifyOnAttachBaseContext(base);
    }
}