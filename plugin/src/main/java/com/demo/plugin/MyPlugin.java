package com.demo.plugin;


import com.android.annotations.NonNull;
import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MyPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        if (appExtension == null) {
            return;
        }
        appExtension.registerTransform(new MyTransform());// 注册优先于task任务的添加
    }
}
