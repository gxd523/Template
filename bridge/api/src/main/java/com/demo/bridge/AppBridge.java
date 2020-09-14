package com.demo.bridge;

import android.content.Context;

import com.demo.annotation.IBridge;

/**
 * Created by guoxiaodong on 2020/9/8 21:48
 */
public interface AppBridge extends IBridge {
    void launchDetailActivity(Context context);
}
