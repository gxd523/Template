package com.demo.app;

import android.content.Context;
import android.content.Intent;

import com.demo.annotation.Bridge;
import com.demo.bridge.AppBridge;

@Bridge
public class AppBridgeImpl implements AppBridge {
    @Override
    public void launchFirstActivity(Context context) {
        Intent intent = new Intent(context, FirstActivity.class);
        context.startActivity(intent);
    }
}
