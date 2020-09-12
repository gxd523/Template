package com.demo.bridge;

import android.app.Application;

import java.util.Set;

/**
 * Created by guoxiaodong on 2020/9/10 20:26
 */
public class ModuleApplicationManager {
    public static void init(Application application) {
        try {
            Set<String> classSet = ClassUtil.getGenerateBridgeClassName(application, "com.demo.generate.moduleapplication");
            for (String classString : classSet) {
                Class<?> aClass = Class.forName(classString);
                Object o = aClass.newInstance();
                aClass.getMethod("addObserver", ApplicationLifecycleOwner.class).invoke(o, ApplicationLifecycleOwner.INSTANCE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}