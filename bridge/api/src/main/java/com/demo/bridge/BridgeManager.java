package com.demo.bridge;

import android.app.Application;

import com.demo.annotation.IBridge;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by guoxiaodong on 2020/9/8 21:49
 */
public class BridgeManager {
    public static final String GENERATE_PACKAGE = "com.demo.generate.bridge";
    private static Map<String, IBridge> bridgeMap = new HashMap<>();

    public static void init(Application application) {
        try {
            Set<String> bridgeGenerateClassNameSet = ClassUtil.getGenerateBridgeClassName(application, GENERATE_PACKAGE);
            for (String bridgeClassName : bridgeGenerateClassNameSet) {
                Class<?> moduleBridgeClass = Class.forName(bridgeClassName);
                Object moduleBridge = moduleBridgeClass.newInstance();
                moduleBridgeClass.getMethod("addBridge", Map.class).invoke(moduleBridge, bridgeMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T getBridge(Class<T> tClass) {
        IBridge bridge = bridgeMap.get(tClass.getName());
        if (bridge == null) {
            return null;
        }
        return (T) bridge;
    }
}
