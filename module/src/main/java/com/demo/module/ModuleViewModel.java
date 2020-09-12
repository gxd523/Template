package com.demo.module;

import android.app.Application;
import android.view.View;

import com.demo.base.BaseViewModel;
import com.demo.bridge.AppBridge;
import com.demo.bridge.BridgeManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by guoxiaodong on 2020/9/9 10:23
 */
public final class ModuleViewModel extends BaseViewModel {
    public MutableLiveData<String> title = new MutableLiveData<>();

    public ModuleViewModel(@NonNull Application application) {
        super(application);
    }

    public void onClick(View view) {
        AppBridge appBridge = BridgeManager.getBridge(AppBridge.class);
        if (appBridge != null) {
            appBridge.launchFirstActivity(view.getContext());
        }
    }
}