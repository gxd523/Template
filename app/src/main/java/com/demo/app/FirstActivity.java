package com.demo.app;

import android.os.Bundle;

import com.demo.base.BaseActivity;

public class FirstActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getContentViewId() {
        return 0;
    }
}