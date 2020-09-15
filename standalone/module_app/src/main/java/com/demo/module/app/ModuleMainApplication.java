package com.demo.module.app;

import com.demo.annotation.AppSpec;
import com.demo.base.BaseApplication;
import com.demo.base.TaskManager;

@AppSpec
public class ModuleMainApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        TaskManager.INSTANCE.addTask(
                () -> {
                    // TODO: 2020/9/15 添加异步初始化
                },
                () -> {
                    // TODO: 2020/9/15 添加异步初始化
                }
        );
        TaskManager.INSTANCE.executeTask();
    }
}