package com.demo.bridge;

import com.demo.annotation.IBridge;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by guoxiaodong on 2020/9/8 21:49
 */
public class BridgeManager {
    private static Map<String, IBridge> bridgeMap = new HashMap<>();

    public static void init(Set<String> generateClassSet) throws Exception {
        for (String generateClass : generateClassSet) {
            if (!generateClass.startsWith("com.demo.generate.bridge")) {
                continue;
            }
            Class<?> moduleBridgeClass = Class.forName(generateClass);
            Object moduleBridge = moduleBridgeClass.newInstance();
            moduleBridgeClass.getMethod("addBridge", Map.class).invoke(moduleBridge, bridgeMap);
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
