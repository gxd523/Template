package com.demo.module.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.demo.base.BaseViewModel;
import com.demo.module.ModuleActivity;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by guoxiaodong on 2020/9/10 09:21
 */
public final class ModuleMainViewModel extends BaseViewModel {
    public MutableLiveData<String> title = new MutableLiveData<>();

    public ModuleMainViewModel(@NonNull Application application) {
        super(application);
    }

    public void onClick(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, ModuleActivity.class);
        context.startActivity(intent);
    }
}