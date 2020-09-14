package com.demo.app;

import android.content.Context;
import android.content.Intent;

import com.demo.annotation.Bridge;
import com.demo.app.detail.DetailActivity;
import com.demo.bridge.AppBridge;

@Bridge
public class AppBridgeImpl implements AppBridge {
    @Override
    public void launchDetailActivity(Context context) {
        Intent intent = new Intent(context, DetailActivity.class);
        context.startActivity(intent);
    }
}
