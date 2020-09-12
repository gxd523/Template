package com.demo.bridge;

import android.content.Context;

import com.demo.annotation.IBridge;
import com.demo.bean.ModuleBean;

import io.reactivex.Single;

/**
 * Created by guoxiaodong on 2020/9/8 21:49
 */
public interface ModuleBridge extends IBridge {
    Single<ModuleBean> requestData();

    void launchModuleActivity(Context context);
}
