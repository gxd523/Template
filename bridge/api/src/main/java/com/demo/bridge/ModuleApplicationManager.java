package com.demo.bridge;

import java.util.Set;

/**
 * Created by guoxiaodong on 2020/9/10 20:26
 */
public class ModuleApplicationManager {
    public static void init(Set<String> generateClassSet) throws Exception {
        for (String generateClass : generateClassSet) {
            if (!generateClass.startsWith("com.demo.generate.lifecycle")) {
                continue;
            }
            Class<?> aClass = Class.forName(generateClass);
            Object o = aClass.newInstance();
            aClass.getMethod("addObserver", ApplicationLifecycleOwner.class).invoke(o, ApplicationLifecycleOwner.INSTANCE);
        }
    }
}